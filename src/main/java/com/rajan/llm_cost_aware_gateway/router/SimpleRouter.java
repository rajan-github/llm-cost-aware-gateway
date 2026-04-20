package com.rajan.llm_cost_aware_gateway.router;

import com.rajan.llm_cost_aware_gateway.controlplane.models.Request;
import com.rajan.llm_cost_aware_gateway.dataplane.llm_clients.GeminiClient;
import com.rajan.llm_cost_aware_gateway.dataplane.llm_clients.OpenAIClient;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLMRequest;
import com.rajan.llm_cost_aware_gateway.enums.BudgetDecision;
import com.rajan.llm_cost_aware_gateway.executionplan.BudgetExecutionPlan;
import com.rajan.llm_cost_aware_gateway.executionplan.ExecutionPlan;
import com.rajan.llm_cost_aware_gateway.executionplan.FallbackExecutionPlan;
import com.rajan.llm_cost_aware_gateway.executionplan.RejectExecutionPlan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SimpleRouter implements Router {
    @Override
    public ExecutionPlan route(Request request, BudgetDecision budgetDecision) {
        log.info("Received request {}, budgetDecision {}", request, budgetDecision);
        if (request == null || budgetDecision == null) {
            throw new IllegalArgumentException("request and budgetDecision cannot be null");
        }
        final var requestBuilder = LLMRequest.builder()
                .requestId(request.getIdempotencyKey())
                .prompt(request.getPrompt())
                .maxTokens(request.getMaxTokens())
                .temperature(0.0);

        return switch (budgetDecision) {
            case BudgetDecision.REJECT -> new RejectExecutionPlan(null, null);
            case BudgetDecision.DOWNGRADE ->
                    new BudgetExecutionPlan(new OpenAIClient(), requestBuilder.model("OPENAI").build());
            case BudgetDecision.ACCEPT ->
                    new FallbackExecutionPlan(List.of(new GeminiClient()), requestBuilder.model("Gemini").build());
        };
    }
}
