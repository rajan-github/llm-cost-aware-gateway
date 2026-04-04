package com.rajan.llm_cost_aware_gateway.ledger;

import com.rajan.llm_cost_aware_gateway.enums.LEDGER_STATE;

import java.util.UUID;

public interface DBLedger {
    void insert(String orgId, UUID requestId, long estimatedTokens, int actualTokens, LEDGER_STATE state);
}
