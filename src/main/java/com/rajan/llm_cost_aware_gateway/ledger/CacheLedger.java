package com.rajan.llm_cost_aware_gateway.ledger;

import com.rajan.llm_cost_aware_gateway.controlplane.models.TokenStats;

public interface CacheLedger {
    TokenStats getTokenStats(final String orgId, final String endpoint, final String model);
    void updateTokenStats(final String orgId, final String endpoint, final String model, final TokenStats stats);
}
