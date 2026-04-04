package com.rajan.llm_cost_aware_gateway.controlplane.budget_manager;

import com.rajan.llm_cost_aware_gateway.enums.LEDGER_STATE;
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
        if (!cacheLedger.reserveTokens(orgId, estimatedTokens)) {
            return false;
        }
        dbLedger.insert(orgId, requestId, estimatedTokens, 0, LEDGER_STATE.RESERVE);
        return true;
    }

    @Override
    public void commit(String orgId, UUID requestId, long actualTokens) {
        log.info("commit is invoked for orgId: {}, requestId: {}", orgId, requestId);

    }

    @Override
    public void refund(String orgId, UUID requestId, long refundTokens) {

    }
}
