package com.rajan.llm_cost_aware_gateway.controlplane.models;

import lombok.Data;

import java.util.UUID;

@Data
public class Request {
    private String orgId;
    private String endpoint;
    private String model;
    private long maxTokens;
    private String prompt;
    private UUID idempotencyKey;
}
