package com.rajan.llm_cost_aware_gateway.controlplane.models;

public record IdempotencyResult(Status status, String requestHash, Response response, long tokensUsed, long updatedAt) {
    public enum Status {
        NEW,
        IN_PROGRESS,
        COMPLETED
    }
}
