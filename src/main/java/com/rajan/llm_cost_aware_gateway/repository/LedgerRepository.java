package com.rajan.llm_cost_aware_gateway.repository;


import com.rajan.llm_cost_aware_gateway.entities.LedgerKey;
import com.rajan.llm_cost_aware_gateway.entities.TokenLedger;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface LedgerRepository extends CrudRepository<TokenLedger, LedgerKey> {
    @Modifying
    @Query(value = """
                INSERT INTO token_ledger (request_id, ledger_state, org_id, tokens, timestamp)
                VALUES (:requestId, :state, :orgId, :tokens, :timestamp)
                ON CONFLICT (request_id, ledger_state, org_id)
                DO NOTHING
            """, nativeQuery = true)
    void insertIgnoreDuplicate(@Param("requestId") UUID requestId,
                               @Param("state") String state,
                               @Param("orgId") String orgId,
                               @Param("tokens") long tokens,
                               @Param("timestamp") LocalDateTime timestamp);


    @Query(value = """
                SELECT * FROM token_ledger 
                WHERE request_id = :requestId 
                AND ledger_state = 'RESERVED' 
                AND org_id = :orgId
            """, nativeQuery = true)
    TokenLedger findReserved(@Param("orgId") String orgId, @Param("requestId") UUID requestId
    );


    @Query(value = """
                SELECT COALESCE(SUM(tokens), 0) 
                FROM token_ledger 
                WHERE org_id = :orgId
            """, nativeQuery = true)
    long sumUsageByOrgId(@Param("orgId") String orgId);
}
