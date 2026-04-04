package com.rajan.llm_cost_aware_gateway.entities;

import com.rajan.llm_cost_aware_gateway.enums.LEDGER_STATE;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class LedgerKey {
    private UUID requestId;
    private LEDGER_STATE ledgerState;
    private String orgId;
}
