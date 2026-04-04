package com.rajan.llm_cost_aware_gateway.ledger;

import com.rajan.llm_cost_aware_gateway.controlplane.models.TokenStats;

import java.util.UUID;

public interface CacheLedger {
    TokenStats getTokenStats(final String orgId, final String endpoint, final String model);

    void updateTokenStats(final String orgId, final String endpoint, final String model, final TokenStats stats);

    boolean reserveTokens(String orgId, long estimatedTokens);
    boolean refundTokens(String orgId, long tokens);
}
