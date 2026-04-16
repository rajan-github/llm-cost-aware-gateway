package com.rajan.llm_cost_aware_gateway.controlplane.models;

import com.rajan.llm_cost_aware_gateway.enums.Endpoints;

public record Response(String prompt, String orgId, Endpoints endpoint, String response, long tokens, String modelName) {

}
