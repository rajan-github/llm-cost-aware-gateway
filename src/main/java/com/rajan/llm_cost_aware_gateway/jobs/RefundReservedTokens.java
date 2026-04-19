package com.rajan.llm_cost_aware_gateway.jobs;

import com.rajan.llm_cost_aware_gateway.constants.CommonConstants;
import com.rajan.llm_cost_aware_gateway.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RefundReservedTokens {
    private final RedissonClient redisClient;

    @Autowired
    public RefundReservedTokens(final RedissonClient client) {
        this.redisClient = client;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void refundReservedTokens() {
        log.info("Refunding Reserved Tokens");
        final var luaScript = """
                local data = redis.call('HMGET', KEYS[1], 'amount', 'timestamp', 'budgetKey')
                
                if not data[1] then
                    redis.call('ZREM', KEYS[2], KEYS[1])
                    return 0
                end
                
                local amount = tonumber(data[1])
                local timestamp = tonumber(data[2])
                local budgetKey = data[3]
                local now = redis.call('TIME')[1]
                
                if now - timestamp < tonumber(ARGV[1]) then
                    return 0
                end
                
                redis.call('INCRBY', budgetKey, amount)
                redis.call('DEL', KEYS[1])
                redis.call('ZREM', KEYS[2], KEYS[1])
                return 1
                """;

        long endValue = Instant.now().minus(5L, ChronoUnit.MINUTES).getEpochSecond();
        final var reservationKeys = redisClient.getScoredSortedSet("reservations:index").valueRange(0, true, endValue, true, 0, 100);

        final var rScript = redisClient.getScript();
        for (var reserveKey : reservationKeys) {
            var reserveKeyStr = (String) reserveKey;
            rScript.eval(RScript.Mode.READ_WRITE, luaScript, RScript.ReturnType.LONG, List.of(reserveKeyStr, "reservations:index"), 300);
        }
    }


    private String extractOrgId(String reserveKey) {
        String[] parts = reserveKey.split(CommonConstants.KEY_SEPARATOR);
        return parts[1];
    }
}
