package com.rajan.llm_cost_aware_gateway.controlplane.models;

import com.rajan.llm_cost_aware_gateway.enums.LEDGER_STATE;

import java.time.LocalDateTime;
import java.util.UUID;

public record LedgerEntry(UUID id, UUID requestId, String orgId, LEDGER_STATE state, long tokens, LocalDateTime timestamp) {
}
