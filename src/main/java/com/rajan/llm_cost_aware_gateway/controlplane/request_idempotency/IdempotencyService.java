package com.rajan.llm_cost_aware_gateway.controlplane.request_idempotency;

import com.rajan.llm_cost_aware_gateway.controlplane.models.IdempotencyResult;
import com.rajan.llm_cost_aware_gateway.controlplane.models.Request;
import com.rajan.llm_cost_aware_gateway.controlplane.models.Response;

import java.util.Optional;

public interface IdempotencyService {
    Optional<IdempotencyResult> preHandle(Request request);

    void onSuccess(Request request, Response response, long tokensUsed);

    void onFailure(Request request);
}
