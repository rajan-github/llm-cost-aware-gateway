package com.rajan.llm_cost_aware_gateway.jobs;

import com.rajan.llm_cost_aware_gateway.cache.Cache;
import com.rajan.llm_cost_aware_gateway.constants.CommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RefillBudget {
    private static final Long MAX_TOKENS = 100000L;
    private final Cache cache;

    @Autowired
    public RefillBudget(Cache cache) {
        this.cache = cache;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void refillBudgets() {
        for (String orgId : getAllArgs()) {
            String key = CommonConstants.BUDGET_KEY + orgId;
            cache.setValue(key, MAX_TOKENS);
        }
    }

    private List<String> getAllArgs() {
        return List.of();
    }
}
