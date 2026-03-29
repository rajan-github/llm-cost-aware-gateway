package com.rajan.llm_cost_aware_gateway.dataplane.llm_clients;

import com.rajan.llm_cost_aware_gateway.dataplane.models.LLMRequest;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLMResponseChunk;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLmResponse;

import java.util.UUID;
import java.util.stream.Stream;

public class GeminiClient implements LLmClient{
    @Override
    public LLmResponse complete(LLMRequest request) {
        return null;
    }

    @Override
    public Stream<LLMResponseChunk> stream(LLMRequest request) {
        return Stream.empty();
    }

    @Override
    public void cancel(UUID requestId) {

    }
}
