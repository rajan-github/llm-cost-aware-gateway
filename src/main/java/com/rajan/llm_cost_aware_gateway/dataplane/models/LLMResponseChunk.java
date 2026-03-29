package com.rajan.llm_cost_aware_gateway.dataplane.models;

import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class LLMResponseChunk {
    private UUID requestId;
    private String delta;
    private boolean isFinal;
    private LLmResponse.Usage usageSoFar;
}
