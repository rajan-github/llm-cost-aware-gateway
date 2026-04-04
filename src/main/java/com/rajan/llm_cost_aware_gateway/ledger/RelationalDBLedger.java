package com.rajan.llm_cost_aware_gateway.ledger;

import com.rajan.llm_cost_aware_gateway.entities.LedgerKey;
import com.rajan.llm_cost_aware_gateway.entities.TokenLedger;
import com.rajan.llm_cost_aware_gateway.enums.LEDGER_STATE;
import com.rajan.llm_cost_aware_gateway.repository.LedgerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;


@Slf4j
@Component
public class RelationalDBLedger implements DBLedger {
    private LedgerRepository<TokenLedger, LedgerKey> ledgerRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public void insert(String orgId, UUID requestId, long tokens, LEDGER_STATE state) {
        log.info("insert is invoked with orgId: {}, requestId: {}, tokens: {} and state: {}", orgId, requestId, tokens, state);
        ledgerRepository.insertIgnoreDuplicate(requestId, state.name(), orgId, tokens, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    @Override
    public long sumUsageByOrgId(String orgId) {
        log.info("sumUsageByOrgId is invoked with orgId: {}", orgId);
        return ledgerRepository.sumUsageByOrgId(orgId);
    }

    @Transactional(readOnly = true)
    @Override
    public TokenLedger getReserveEntry(UUID requestId, String orgId) {
        log.info("getReserveEntry is invoked with requestId: {}", requestId);
        return ledgerRepository.findReserved(orgId, requestId);
    }
}
