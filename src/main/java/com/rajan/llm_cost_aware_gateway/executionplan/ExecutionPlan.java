package com.rajan.llm_cost_aware_gateway.executionplan;

import com.rajan.llm_cost_aware_gateway.dataplane.models.LLmResponse;

public interface ExecutionPlan {
    LLmResponse apply() throws InterruptedException;
}
