package com.rajan.llm_cost_aware_gateway.controlplane.budget_manager;

import com.rajan.llm_cost_aware_gateway.controlplane.models.TokenEstimate;

public interface BudgetManager {
    boolean tryReserve(String orgId, String endpoint, String model, double estimatedP95);
    void commit(String orgId, String endpoint, String model, double actualUsage);
    void refund(String orgId, String endpoint, String model, double actualUsage);
}
