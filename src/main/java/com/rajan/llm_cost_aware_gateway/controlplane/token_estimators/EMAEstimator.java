package com.rajan.llm_cost_aware_gateway.controlplane.token_estimators;

import com.rajan.llm_cost_aware_gateway.controlplane.models.Request;
import com.rajan.llm_cost_aware_gateway.controlplane.models.TokenEstimate;
import com.rajan.llm_cost_aware_gateway.controlplane.models.TokenStats;
import com.rajan.llm_cost_aware_gateway.ledger.CacheLedger;
import com.rajan.llm_cost_aware_gateway.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EMAEstimator implements TokenEstimator {
    private final double ALPHA = 0.2;

    private final CacheLedger cacheLedger;

    @Autowired
    public EMAEstimator(CacheLedger cacheLedger) {
        this.cacheLedger = cacheLedger;
    }

    @Override
    public TokenEstimate estimate(Request request) {
        long inputTokens = Utils.getTokens(request.getPrompt());
        double confidence = 0.3;
        final var lastStatsOptional = cacheLedger.getTokenStats(request.getOrgId(), request.getEndpoint(), request.getModel());
        if (lastStatsOptional.isEmpty()) {
            double p50 = inputTokens + (0.5) * request.getMaxTokens();
            double p95 = inputTokens + (1.0) * request.getMaxTokens();
            return new TokenEstimate(p50, p95, confidence);
        }
        final var lastStats = lastStatsOptional.get();
        double estimatedOutputP50 = Math.min(request.getMaxTokens(), lastStats.p50());
        double estimatedOutputP95 = Math.min(request.getMaxTokens(), lastStats.p95());
        confidence = 1 / (1 + lastStats.error());
        return new TokenEstimate(estimatedOutputP50, estimatedOutputP95, confidence);
    }

    @Override
    public void update(long actualUsage, TokenEstimate estimate, Request request) {
        double p50 = ALPHA * actualUsage + (1 - ALPHA) * estimate.p50();
        double p95 = ALPHA * actualUsage + (1 - ALPHA) * estimate.p95();
        double error = Math.abs(actualUsage - estimate.p50()) / Math.max(1, actualUsage);
        final var lastStatsOptional = cacheLedger.getTokenStats(request.getOrgId(), request.getEndpoint(), request.getModel());
        long count = lastStatsOptional.map(tokenStats -> tokenStats.count() + 1).orElse(0L);
        final var stats = new TokenStats(count, p50, p95, error);
        this.cacheLedger.updateTokenStats(request.getOrgId(), request.getEndpoint(), request.getModel(), stats);
    }
}
