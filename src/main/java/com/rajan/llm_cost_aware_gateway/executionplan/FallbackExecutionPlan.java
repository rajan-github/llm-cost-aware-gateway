package com.rajan.llm_cost_aware_gateway.executionplan;

import com.rajan.llm_cost_aware_gateway.dataplane.llm_clients.LLmClient;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLMRequest;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLmResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@AllArgsConstructor
public class FallbackExecutionPlan implements ExecutionPlan {
    private final List<LLmClient> clients;
    private final LLMRequest request;

    @Override
    public LLmResponse apply() throws InterruptedException {
        for (LLmClient client : clients) {
            try {
                return client.complete(request);
            } catch (Exception e) {
                //try next;
            }
        }
        throw new RuntimeException("All fallback models failed!");
    }

    @Override
    public String getPlanName() {
        return "fallback-plan";
    }
}
