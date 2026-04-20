package com.rajan.llm_cost_aware_gateway.controllers;

import com.rajan.llm_cost_aware_gateway.controlplane.models.Request;
import com.rajan.llm_cost_aware_gateway.controlplane.models.Response;
import com.rajan.llm_cost_aware_gateway.orchestrator.RequestOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/llm")
public class LlmController {

    private final RequestOrchestrator orchestrator;

    public LlmController(RequestOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/generate")
    public ResponseEntity<Response> generate(@RequestBody Request request) throws Exception {
        Response response = orchestrator.handle(request);
        return ResponseEntity.ok(response);
    }
}