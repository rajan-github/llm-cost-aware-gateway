package com.rajan.llm_cost_aware_gateway.dataplane.models;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public class LLmResponse {
    private String model;
    private Usage usage;
    private UUID requestId;
    private List<Choice> choices;
    private boolean isPartial;
    private boolean isTruncated;

    @AllArgsConstructor
    public static class Usage {
        private int inputTokens;
        private int outputTokens;
        private int totalTokens;
        private double estimatedCosts;
    }

    @AllArgsConstructor
    public static class Choice {
        private FinishReason finishReason;
        private Message message;
    }

    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    public enum FinishReason {
        STOP,
        LENGTH,
        CONTENT_FILTER,
        NULL,
        ERROR
    }
}



