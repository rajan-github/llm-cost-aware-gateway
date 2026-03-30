package com.rajan.llm_cost_aware_gateway.dataplane.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class LLMResponseChunk {
    private UUID requestId;
    private String delta;
    private boolean isFinal;
    private LLmResponse.Usage usageSoFar;
}
