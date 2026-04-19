package com.rajan.llm_cost_aware_gateway.dataplane.llm_clients;

import com.rajan.llm_cost_aware_gateway.dataplane.models.LLMRequest;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLMResponseChunk;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLmResponse;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Stream;

@Service(value = "GEMINI")
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
