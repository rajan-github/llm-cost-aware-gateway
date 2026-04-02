package com.rajan.llm_cost_aware_gateway.ledger;

import com.rajan.llm_cost_aware_gateway.controlplane.models.TokenStats;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.rajan.llm_cost_aware_gateway.utils.Utils.createLedgerKey;

@Component
public class InMemoryCacheLedger implements CacheLedger {
    private final Map<String, TokenStats> tokenStats;

    public InMemoryCacheLedger() {
        this.tokenStats = new ConcurrentHashMap<>();
    }


    @Override
    public TokenStats getTokenStats(String orgId, String endpoint, String model) {
        final var key = createLedgerKey(orgId, endpoint, model);
        return tokenStats.get(key);
    }

    @Override
    public void updateTokenStats(String orgId, String endpoint, String model, TokenStats stats) {
        final var key = createLedgerKey(orgId, endpoint, model);
        tokenStats.put(key, stats);
    }
}
