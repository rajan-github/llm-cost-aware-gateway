package com.rajan.llm_cost_aware_gateway.ledger;

import com.rajan.llm_cost_aware_gateway.entities.LedgerKey;
import com.rajan.llm_cost_aware_gateway.entities.TokenLedger;
import com.rajan.llm_cost_aware_gateway.enums.LedgerState;
import com.rajan.llm_cost_aware_gateway.repository.LedgerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;


@Slf4j
@Component
public class RelationalDBLedger implements DBLedger {

    @Autowired
    private final LedgerRepository ledgerRepository;

    public RelationalDBLedger(LedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public void insert(String orgId, UUID requestId, long tokens, LedgerState state) {
        log.info("insert is invoked with orgId: {}, requestId: {}, tokens: {} and state: {}", orgId, requestId, tokens, state);
        try {
            ledgerRepository.save(new TokenLedger(new LedgerKey(requestId, state, orgId), tokens, LocalDateTime.now()));
        } catch (DataIntegrityViolationException ex) {
            log.error("insert failed with exception: {}. Ignoring insertion.", ex.getMessage());
        }
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
