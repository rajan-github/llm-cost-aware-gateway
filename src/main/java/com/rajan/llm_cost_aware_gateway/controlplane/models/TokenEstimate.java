package com.rajan.llm_cost_aware_gateway.controlplane.models;


public record TokenEstimate(double p50, double p95, double confidence) {

}
