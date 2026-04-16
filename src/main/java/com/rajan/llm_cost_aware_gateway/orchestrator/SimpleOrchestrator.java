package com.rajan.llm_cost_aware_gateway.orchestrator;

import com.rajan.llm_cost_aware_gateway.controlplane.budget_enforcer.BudgetEnforcer;
import com.rajan.llm_cost_aware_gateway.controlplane.budget_manager.BudgetManager;
import com.rajan.llm_cost_aware_gateway.controlplane.models.Request;
import com.rajan.llm_cost_aware_gateway.controlplane.models.Response;
import com.rajan.llm_cost_aware_gateway.controlplane.models.TokenEstimate;
import com.rajan.llm_cost_aware_gateway.controlplane.request_idempotency.IdempotencyService;
import com.rajan.llm_cost_aware_gateway.controlplane.token_estimators.TokenEstimator;
import com.rajan.llm_cost_aware_gateway.enums.BudgetDecision;
import com.rajan.llm_cost_aware_gateway.ledger.CacheLedger;
import com.rajan.llm_cost_aware_gateway.router.Router;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SimpleOrchestrator implements RequestOrchestrator {
    private final IdempotencyService idempotencyService;
    private final BudgetManager budgetManager;
    private final TokenEstimator tokenEstimator;
    private final BudgetEnforcer budgetEnforcer;
    private final CacheLedger cacheLedger;
    private final Router router;

    @Autowired
    public SimpleOrchestrator(IdempotencyService idempotencyService, BudgetManager budgetManager, TokenEstimator tokenEstimator, BudgetEnforcer budgetEnforcer, CacheLedger cacheLedger, Router router) {
        this.idempotencyService = idempotencyService;
        this.budgetManager = budgetManager;
        this.tokenEstimator = tokenEstimator;
        this.budgetEnforcer = budgetEnforcer;
        this.cacheLedger = cacheLedger;
        this.router = router;
    }

    @Override
    public Response handle(Request request) throws Exception {
        log.info("SimpleOrchestrator.handle is invoked with request {}", request);
        final var idempotencyResultOptional = idempotencyService.preHandle(request);
        if (idempotencyResultOptional.isEmpty()) {
            final TokenEstimate estimate = tokenEstimator.estimate(request);

            final BudgetDecision decision = budgetEnforcer.decide(request, estimate, cacheLedger.getRemainingTokens(request.getOrgId()));
            if (decision == BudgetDecision.REJECT) {
                return rejectionResponse(request);
            }
            boolean reserved = budgetManager.tryReserve(request.getOrgId(), (long) estimate.p95(), request.getIdempotencyKey());
            if (!reserved) {
                log.info("SimpleOrchestrator Tokens couldn't be reserved. Thus rejecting the request {}", request);
                return rejectionResponse(request);
            }
            final var executionPlan = router.route(request, decision);
            try {
                final var llmResponse = executionPlan.apply();
                var tokensUsed = llmResponse.getUsage().getTotalTokens();
                budgetManager.commit(request.getOrgId(), request.getIdempotencyKey(), tokensUsed);
                var responseMsg = (llmResponse.getChoices() == null || llmResponse.getChoices().isEmpty()) ? null : llmResponse.getChoices().get(0).getMessage();
                final var response = new Response(request.getPrompt(), request.getOrgId(), request.getEndpoint(), responseMsg == null ? null : responseMsg.getContent(), tokensUsed, llmResponse.getModel());
                idempotencyService.onSuccess(request, response, tokensUsed);
                return response;
            } catch (Exception e) {
                budgetManager.refund(request.getOrgId(), request.getIdempotencyKey(), (long) estimate.p95());
                idempotencyService.onFailure(request);
            }
            return rejectionResponse(request);
        } else {
            var idempotencyResult = idempotencyResultOptional.get();
            log.info("SimpleOrchestrator.handle- request is duplicate and returning cached response for request: {}", request);
            return idempotencyResult.response();
        }
    }

    private Response rejectionResponse(final Request request) {
        return new Response(request.getPrompt(), request.getOrgId(), request.getEndpoint(), null, 0L, null);
    }
}
