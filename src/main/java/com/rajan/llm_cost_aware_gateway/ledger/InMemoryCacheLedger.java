package com.rajan.llm_cost_aware_gateway.ledger;

import com.rajan.llm_cost_aware_gateway.constants.CommonConstants;
import com.rajan.llm_cost_aware_gateway.controlplane.models.TokenStats;
import com.rajan.llm_cost_aware_gateway.enums.Endpoints;
import com.rajan.llm_cost_aware_gateway.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.rajan.llm_cost_aware_gateway.utils.Utils.createLedgerKey;

@Slf4j
@Component
public class InMemoryCacheLedger implements CacheLedger {
    private final RedissonClient redisClient;

    @Autowired
    public InMemoryCacheLedger(RedissonClient redisClient) {
        this.redisClient = redisClient;
    }


    @Override
    public Optional<TokenStats> getTokenStats(String orgId, Endpoints endpoint, String model) {
        log.info("getTokenStats is invoked for orgId: {}, endpoint: {}, model: {}", orgId, endpoint, model);
        final RMap<String, TokenStats> tokenStatsMap = redisClient.getMap(CommonConstants.TOKEN_STATS_KEY);
        final var key = createLedgerKey(orgId, endpoint.name(), model);
        final var tokenStats = tokenStatsMap.get(key);
        return Optional.ofNullable(tokenStats);
    }

    @Override
    public void updateTokenStats(String orgId, Endpoints endpoint, String model, TokenStats stats) {
        log.info("updateTokenStats- updating token stats for orgId: {}, endPoint: {}, model: {}", orgId, endpoint, model);
        final RMap<String, TokenStats> tokenStatsMap = redisClient.getMap(CommonConstants.TOKEN_STATS_KEY);
        final var key = createLedgerKey(orgId, endpoint.name(), model);
        tokenStatsMap.put(key, stats);
    }

    @Override
    public boolean reserveTokens(String orgId, UUID requestId, long estimatedTokens) {
        log.info("Reserving tokens for orgId: {} and estimatedTokens: {}", orgId, estimatedTokens);
        final var lua = """
                     local current = redis.call('GET', KEYS[1])
                     if not current then\s
                         return 0\s
                     end
                
                     if redis.call('EXISTS', KEYS[2]) == 1 then
                        return 1
                     end
                \s
                     current = tonumber(current)
                     local requested=tonumber(ARGV[1])
                     if not current or not requested then return 0 end\s
                \s
                     if current >= requested then
                         redis.call('DECRBY', KEYS[1], requested)
                         local now = redis.call('TIME')[1]\s
                         redis.call('HSET', KEYS[2], 'amount', requested, 'timestamp', now, 'budgetKey', KEYS[1])
                         redis.call('ZADD', 'reservations:index', now, KEYS[2])
                         return 1
                     else\s
                         return 0
                     end
                \s""";

        RScript rScript = redisClient.getScript();
        final String budgetKey = Utils.constructKey(CommonConstants.BUDGET_KEY, orgId);
        final String reserveKey = Utils.constructKey(CommonConstants.RESERVE_KEY, orgId, requestId.toString());
        final long result = rScript.eval(RScript.Mode.READ_WRITE, lua, RScript.ReturnType.LONG, List.of(budgetKey, reserveKey), estimatedTokens);
        if (result == 0) {
            log.warn("Token reservation failed for orgId={} (insufficient or missing budget)", orgId);
            return false;
        }
        return true;
    }

    @Override
    public long refundTokens(String orgId, long tokens) {
        log.info("refundTokens for orgId: {} and refundTokens: {}", orgId, tokens);
        final String key = CommonConstants.BUDGET_KEY + orgId;
        return redisClient.getAtomicLong(key).addAndGet(tokens);
    }

    @Override
    public long deductTokens(String orgId, long tokens) {
        log.info("deductTokens for orgId: {}, tokens: {}", orgId, tokens);
        final String key = CommonConstants.BUDGET_KEY + orgId;
        return redisClient.getAtomicLong(key).addAndGet(-tokens);
    }

    @Override
    public long getRemainingTokens(String orgId) {
        final String key = CommonConstants.BUDGET_KEY + orgId;
        return redisClient.getAtomicLong(CommonConstants.BUDGET_KEY + orgId).get();
    }
}
