package com.rajan.llm_cost_aware_gateway.controlplane.budget_enforcer;

import com.rajan.llm_cost_aware_gateway.controlplane.models.Request;
import com.rajan.llm_cost_aware_gateway.controlplane.models.TokenEstimate;
import com.rajan.llm_cost_aware_gateway.enums.BudgetDecision;

public interface BudgetEnforcer {
    BudgetDecision decide(final Request request, final TokenEstimate estimate, long remaining);
}
