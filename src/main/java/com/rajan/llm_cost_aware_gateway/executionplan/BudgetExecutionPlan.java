package com.rajan.llm_cost_aware_gateway.executionplan;

import com.rajan.llm_cost_aware_gateway.dataplane.llm_clients.LLmClient;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLMRequest;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLmResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@AllArgsConstructor
public class BudgetExecutionPlan implements ExecutionPlan {
    private final LLmClient cheapClient;
    private final LLMRequest llmRequest;

    @Override
    public LLmResponse apply() throws InterruptedException {
        return cheapClient.complete(llmRequest);
    }

    @Override
    public String getPlanName() {
        return "budget-degraged-plan";
    }
}
