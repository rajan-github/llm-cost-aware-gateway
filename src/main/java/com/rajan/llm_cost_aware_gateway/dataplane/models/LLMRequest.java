package com.rajan.llm_cost_aware_gateway.dataplane.models;

import com.rajan.llm_cost_aware_gateway.controlplane.models.Request;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class LLMRequest {
    private String model;
    private String prompt;
    private long maxTokens;
    private double temperature;
    private UUID requestId;
    private Map<String, Object> metadata;
}
