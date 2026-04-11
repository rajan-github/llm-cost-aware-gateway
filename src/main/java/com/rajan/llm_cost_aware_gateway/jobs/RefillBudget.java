package com.rajan.llm_cost_aware_gateway.jobs;

import com.rajan.llm_cost_aware_gateway.constants.CommonConstants;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RefillBudget {
    private static final Long MAX_TOKENS = 100000L;
    private final RedissonClient redisClient;

    @Autowired
    public RefillBudget(final RedissonClient client) {
        this.redisClient = client;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void refillBudgets() {
        for (String orgId : getAllArgs()) {
            String key = CommonConstants.BUDGET_KEY + orgId;
            redisClient.getAtomicLong(key).set(MAX_TOKENS);
        }
    }

    private List<String> getAllArgs() {
        return List.of();
    }
}
