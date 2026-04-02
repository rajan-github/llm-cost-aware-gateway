package com.rajan.llm_cost_aware_gateway.controlplane.router;

import com.rajan.llm_cost_aware_gateway.controlplane.models.BudgetState;
import com.rajan.llm_cost_aware_gateway.controlplane.models.Request;
import com.rajan.llm_cost_aware_gateway.controlplane.models.TokenEstimate;

public interface Router {
    void route(Request request, TokenEstimate estimate, BudgetState state);
}
