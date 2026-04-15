package com.rajan.llm_cost_aware_gateway.entities;

import com.rajan.llm_cost_aware_gateway.enums.LedgerState;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Embeddable
@NoArgsConstructor
public class LedgerKey implements Serializable {
    private UUID requestId;
    private LedgerState ledgerState;
    private String orgId;
}
