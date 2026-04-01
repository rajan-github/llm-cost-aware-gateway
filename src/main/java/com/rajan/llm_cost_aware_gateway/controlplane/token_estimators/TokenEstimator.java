package com.rajan.llm_cost_aware_gateway.controlplane.token_estimators;

import com.rajan.llm_cost_aware_gateway.controlplane.models.Request;
import com.rajan.llm_cost_aware_gateway.controlplane.models.TokenEstimate;

public interface TokenEstimator {
    TokenEstimate estimate(Request request);

    void update(long actualUsage, TokenEstimate estimate);

}
