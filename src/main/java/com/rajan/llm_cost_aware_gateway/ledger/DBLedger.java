package com.rajan.llm_cost_aware_gateway.ledger;

import com.rajan.llm_cost_aware_gateway.controlplane.models.LedgerEntry;
import com.rajan.llm_cost_aware_gateway.entities.TokenLedger;
import com.rajan.llm_cost_aware_gateway.enums.LEDGER_STATE;

import java.util.UUID;

public interface DBLedger {
    void insert(String orgId, UUID requestId, long tokens, LEDGER_STATE state);

    long sumUsageByOrgId(String orgId);

    TokenLedger getReserveEntry(UUID requestId, String orgId);
}
