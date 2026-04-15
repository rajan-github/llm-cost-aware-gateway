package com.rajan.llm_cost_aware_gateway.controlplane.budget_enforcer;

import com.rajan.llm_cost_aware_gateway.controlplane.models.Request;
import com.rajan.llm_cost_aware_gateway.controlplane.models.TokenEstimate;
import com.rajan.llm_cost_aware_gateway.enums.BudgetDecision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BudgetEnforcerImpl implements BudgetEnforcer {

    @Override
    public BudgetDecision decide(Request request, TokenEstimate estimate, long remaining) {
        log.info("BudgetEnforcer.decide(request, estimate, remaining) is invoked with: {}, {}, {}", request, estimate, remaining);
        if (estimate == null) {
            return BudgetDecision.REJECT;
        }
        if (estimate.p95() < remaining) {
            return BudgetDecision.ACCEPT;
        }
        return BudgetDecision.DOWNGRADE;
    }
}
