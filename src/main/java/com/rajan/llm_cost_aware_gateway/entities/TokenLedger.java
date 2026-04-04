package com.rajan.llm_cost_aware_gateway.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Table(name = "token_ledger")
@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenLedger {
    @Id
    private LedgerKey ledgerId;
    private long tokens;
    private LocalDateTime timestamp;
}
