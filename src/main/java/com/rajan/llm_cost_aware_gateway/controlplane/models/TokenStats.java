package com.rajan.llm_cost_aware_gateway.controlplane.models;

public record TokenStats(long count, double p50, double p95, double error) {
}
