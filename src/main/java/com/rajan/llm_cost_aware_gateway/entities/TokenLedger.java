package com.rajan.llm_cost_aware_gateway.entities;

import jakarta.persistence.*;
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
    @EmbeddedId
    private LedgerKey ledgerId;
    private long tokens;
    private LocalDateTime timestamp;
}
