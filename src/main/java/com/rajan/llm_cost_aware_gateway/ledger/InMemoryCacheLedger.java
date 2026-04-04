package com.rajan.llm_cost_aware_gateway.ledger;

import com.rajan.llm_cost_aware_gateway.cache.Cache;
import com.rajan.llm_cost_aware_gateway.constants.CommonConstants;
import com.rajan.llm_cost_aware_gateway.controlplane.models.TokenStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.UUID;

import static com.rajan.llm_cost_aware_gateway.utils.Utils.createLedgerKey;

@Slf4j
@Component
public class InMemoryCacheLedger implements CacheLedger {
    private final Cache cache;
    private final DBLedger dbLedger;

    @Autowired
    public InMemoryCacheLedger(Cache cache, DBLedger dbLedger) {
        this.cache = cache;
        this.dbLedger = dbLedger;
    }


    @Override
    public TokenStats getTokenStats(String orgId, String endpoint, String model) {
        log.info("getTokenStats is invoked for orgId: {}, endpoint: {}, model: {}", orgId, endpoint, model);
        final var key = createLedgerKey(orgId, endpoint, model);
        final var responseOptional = cache.getValue(key);
        if (responseOptional.isPresent()) {
            var value = responseOptional.get();
            if (value instanceof TokenStats) {
                return (TokenStats) value;
            }
            throw new IllegalArgumentException("Value for key: " + key + " is not an instance of TokenStats");
        } else {
            return null;
        }
    }

    @Override
    public void updateTokenStats(String orgId, String endpoint, String model, TokenStats stats) {
        log.info("updateTokenStats- updating token stats for orgId: {}, endPoint: {}, model: {}", orgId, endpoint, model);
        final var key = createLedgerKey(orgId, endpoint, model);
        cache.setValue(key, stats);
    }

    @Override
    public boolean reserveTokens(String orgId, long estimatedTokens) {
        log.info("Reserving tokens for orgId: {} and estimatedTokens: {}", orgId, estimatedTokens);
        if (tryReserveTokens(orgId, estimatedTokens)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean refundTokens(String orgId, long tokens) {
        final String key = CommonConstants.BUDGET_KEY + orgId;
        final var responseOptional = cache.getValue(key);
        if (responseOptional.isPresent()) {
            var value = responseOptional.get();
            if (value instanceof Long) {
                cache.setValue(key, (long) value + tokens);
            } else {
                throw new IllegalArgumentException("Value for key: " + key + " is not an instance of Long");
            }
        } else {
            cache.setValue(key, tokens);
        }
        return true;
    }

    private boolean tryReserveTokens(String orgId, long estimatedTokens) {
        final String key = CommonConstants.BUDGET_KEY + orgId;
        final var responseOptional = cache.getValue(key);
        if (responseOptional.isPresent()) {
            var value = responseOptional.get();
            if (value instanceof Long) {
                long currentTokens = (Long) value;
                if (currentTokens >= estimatedTokens) {
                    cache.setValue(key, currentTokens - estimatedTokens);
                    return true;
                }
            } else {
                log.error("reserveTokens- value found for tokens in cache is not an instance of Long.");
            }
        }
        return false;
    }
}
