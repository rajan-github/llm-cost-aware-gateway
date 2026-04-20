package com.rajan.llm_cost_aware_gateway.controlplane.models;

import com.rajan.llm_cost_aware_gateway.enums.Endpoints;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Request {
    private String orgId;
    private Endpoints endpoint;
    private String model;
    private long maxTokens;
    private String prompt;
    private UUID idempotencyKey;
}
