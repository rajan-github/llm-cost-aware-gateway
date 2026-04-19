package com.rajan.llm_cost_aware_gateway.controlplane.budget_manager;

import com.rajan.llm_cost_aware_gateway.enums.LedgerState;
import com.rajan.llm_cost_aware_gateway.ledger.CacheLedger;
import com.rajan.llm_cost_aware_gateway.ledger.DBLedger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class SimpleBudgetManager implements BudgetManager {
    private final CacheLedger cacheLedger;
    private final DBLedger dbLedger;

    @Autowired
    public SimpleBudgetManager(CacheLedger cacheLedger, DBLedger dbLedger) {
        this.cacheLedger = cacheLedger;
        this.dbLedger = dbLedger;
    }

    @Override
    public boolean tryReserve(String orgId, long estimatedTokens, UUID requestId) {
        log.info("tryReserve is invoked for orgId: {}, requestId: {}", orgId, requestId);
        if (!cacheLedger.reserveTokens(orgId, requestId, estimatedTokens)) {
            return false;
        }
        dbLedger.insert(orgId, requestId, estimatedTokens, LedgerState.RESERVE);
        return true;
    }

    @Override
    public void commit(String orgId, UUID requestId, long actualTokens) {
        log.info("commit is invoked for orgId: {}, requestId: {}", orgId, requestId);
        var reserveEntry = dbLedger.getReserveEntry(requestId, orgId);
        if (reserveEntry != null && reserveEntry.getTokens() < actualTokens) {
            long newVal = cacheLedger.deductTokens(orgId, actualTokens - reserveEntry.getTokens());
            log.info("reserveTokens for orgId: {}, and new tokens value is: {}", orgId, newVal);
        } else {
            if (reserveEntry != null) {
                long refundTokens = Math.max(0L, reserveEntry.getTokens() - actualTokens);
                final var newVal = cacheLedger.refundTokens(orgId, refundTokens);
                log.info("refundTokens for orgId: {}, and new tokens value is: {}", orgId, newVal);
                dbLedger.insert(orgId, requestId, refundTokens, LedgerState.REFUND);
            }
        }
        dbLedger.insert(orgId, requestId, actualTokens, LedgerState.COMMIT);
    }

    @Override
    public void refund(String orgId, UUID requestId, long refundTokens) {
        log.info("refund is invoked for orgId: {}, requestId: {}", orgId, requestId);
        cacheLedger.refundTokens(orgId, refundTokens);
        dbLedger.insert(orgId, requestId, refundTokens, LedgerState.REFUND);
    }
}
