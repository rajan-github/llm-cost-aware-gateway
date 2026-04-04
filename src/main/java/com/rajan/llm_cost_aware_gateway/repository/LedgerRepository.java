package com.rajan.llm_cost_aware_gateway.repository;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface LedgerRepository<TokenLedger, LedgerKey> extends CrudRepository<TokenLedger, LedgerKey> {
    @Modifying
    @Query(value = """
    INSERT INTO token_ledger (request_id, state, org_id, tokens, timestamp)
    VALUES (:requestId, :state, :orgId, :tokens, :timestamp)
    ON CONFLICT (request_id, state, org_id)
    DO NOTHING
""", nativeQuery = true)
    void insertIgnoreDuplicate(UUID requestId, String state, String orgId, long tokens, LocalDateTime timestamp);
}
