package com.rajan.llm_cost_aware_gateway.orchestrator;

import com.rajan.llm_cost_aware_gateway.controlplane.models.Request;
import com.rajan.llm_cost_aware_gateway.controlplane.models.Response;

public interface RequestOrchestrator {
    Response handle(Request request) throws Exception;
}
