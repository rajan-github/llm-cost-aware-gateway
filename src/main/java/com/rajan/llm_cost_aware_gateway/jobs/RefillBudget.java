package com.rajan.llm_cost_aware_gateway.jobs;

import com.rajan.llm_cost_aware_gateway.constants.CommonConstants;
import com.rajan.llm_cost_aware_gateway.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
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
        log.info("Refilling budgets for all-org");
        for (String orgId : getAllArgs()) {
            String key = Utils.constructKey(CommonConstants.BUDGET_KEY, orgId);
            redisClient.getAtomicLong(key).set(MAX_TOKENS);
        }
    }

    private List<String> getAllArgs() {
        return List.of(
                "org-123",
                "org-234",
                "org-345"
        );
    }
}
