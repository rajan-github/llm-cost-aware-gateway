package com.rajan.llm_cost_aware_gateway.dataplane.models;

import java.util.UUID;

public class LLMResponseChunk {
    private UUID requestId;
    private String delta;
    private boolean isFinal;
    private LLmResponse.Usage usageSoFar;
}
