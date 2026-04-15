package com.rajan.llm_cost_aware_gateway.ledger;

import com.rajan.llm_cost_aware_gateway.entities.TokenLedger;
import com.rajan.llm_cost_aware_gateway.enums.LedgerState;

import java.util.UUID;

public interface DBLedger {
    void insert(String orgId, UUID requestId, long tokens, LedgerState state);

    long sumUsageByOrgId(String orgId);

    TokenLedger getReserveEntry(UUID requestId, String orgId);
}
