package com.rajan.llm_cost_aware_gateway.controlplane.budget_manager;

import java.util.UUID;

public interface BudgetManager {
    boolean tryReserve(String orgId, long estimatedTokens, UUID requestId);

    void commit(String orgId, UUID requestId, long actualTokens);

    void refund(String orgId, UUID requestId, long refundTokens);
}
