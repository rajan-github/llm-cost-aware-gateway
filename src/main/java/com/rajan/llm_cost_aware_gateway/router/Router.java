package com.rajan.llm_cost_aware_gateway.router;

import com.rajan.llm_cost_aware_gateway.controlplane.models.Request;
import com.rajan.llm_cost_aware_gateway.enums.BudgetDecision;
import com.rajan.llm_cost_aware_gateway.executionplan.ExecutionPlan;

public interface Router {
    ExecutionPlan route(Request request, BudgetDecision budgetDecision);
}
