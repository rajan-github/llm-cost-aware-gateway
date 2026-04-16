package com.rajan.llm_cost_aware_gateway.executionplan;

import com.rajan.llm_cost_aware_gateway.dataplane.llm_clients.LLmClient;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLMRequest;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLmResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
public class RejectExecutionPlan implements ExecutionPlan {
    private final LLmClient llmClient;
    private final LLMRequest request;

    @Autowired
    public RejectExecutionPlan(LLmClient llmClient, LLMRequest request) {
        this.llmClient = llmClient;
        this.request = request;
    }

    @Override
    public LLmResponse apply() throws InterruptedException {
        log.info("Executing Simple Execution Plan using client: {}", llmClient);
        if (llmClient == null) {
            log.error("The client request doesn't have sufficient tokens to execute. Rejecting the request");
            return LLmResponse.builder().usage(new LLmResponse.Usage(0, 0, 0, 0.0))
                    .choices(List.of())
                    .isTruncated(true)
                    .build();

        } else {
            return llmClient.complete(request);
        }
    }

    @Override
    public String getPlanName() {
        return "reject-execution-plan";
    }
}
