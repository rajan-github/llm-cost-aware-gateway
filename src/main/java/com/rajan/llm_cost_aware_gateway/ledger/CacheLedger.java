package com.rajan.llm_cost_aware_gateway.ledger;

import com.rajan.llm_cost_aware_gateway.controlplane.models.TokenStats;
import com.rajan.llm_cost_aware_gateway.enums.Endpoints;

import java.util.Optional;
import java.util.UUID;

public interface CacheLedger {
    Optional<TokenStats> getTokenStats(final String orgId, final Endpoints endpoint, final String model);

    void updateTokenStats(final String orgId, final Endpoints endpoint, final String model, final TokenStats stats);

    boolean reserveTokens(String orgId, UUID requestId, long estimatedTokens);

    long refundTokens(String orgId, long tokens);

    long deductTokens(String orgId, long tokens);

    long getRemainingTokens(String orgId);
}
