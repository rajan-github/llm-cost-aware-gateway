package com.rajan.llm_cost_aware_gateway.dataplane.llm_clients;

import com.rajan.llm_cost_aware_gateway.dataplane.models.LLMRequest;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLMResponseChunk;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLmResponse;

import java.util.UUID;
import java.util.stream.Stream;

public interface LLmClient {
    LLmResponse complete(LLMRequest request) throws InterruptedException;

    Stream<LLMResponseChunk> stream(LLMRequest request);

    void cancel(UUID requestId);
}
