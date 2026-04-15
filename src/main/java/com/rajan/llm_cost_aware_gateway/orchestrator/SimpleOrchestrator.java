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

    @Autowired
    public SimpleOrchestrator(IdempotencyService idempotencyService, BudgetManager budgetManager, TokenEstimator tokenEstimator, BudgetEnforcer budgetEnforcer, CacheLedger cacheLedger) {
        this.idempotencyService = idempotencyService;
        this.budgetManager = budgetManager;
        this.tokenEstimator = tokenEstimator;
        this.budgetEnforcer = budgetEnforcer;
        this.cacheLedger = cacheLedger;
    }

    @Override
    public Response handle(Request request) throws Exception {
        log.info("SimpleOrchestrator.handle is invoked with request {}", request);
        final var idempotencyResultOptional = idempotencyService.preHandle(request);
        if (idempotencyResultOptional.isEmpty()) {
            final TokenEstimate estimate = tokenEstimator.estimate(request);
            final BudgetDecision decision = budgetEnforcer.decide(request, estimate, cacheLedger.getRemainingTokens(request.getOrgId()));
            return null;
            //TODO: call on success of idempotency service
        } else {
            var idempotencyResult = idempotencyResultOptional.get();
            log.info("SimpleOrchestrator.handle- request is duplicate and returning cached response for request: {}", request);
            return idempotencyResult.response();
        }
    }
}
