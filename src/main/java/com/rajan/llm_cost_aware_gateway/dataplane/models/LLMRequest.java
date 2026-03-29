package com.rajan.llm_cost_aware_gateway.dataplane.models;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class LLMRequest {
    private String model;
    private String prompt;
    private int maxTokens;
    private double temperature;
    private Map<String, Object> metadata;
}
