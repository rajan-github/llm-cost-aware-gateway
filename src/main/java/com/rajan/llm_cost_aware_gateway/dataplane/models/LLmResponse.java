package com.rajan.llm_cost_aware_gateway.dataplane.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class LLmResponse {
    private String model;
    private Usage usage;
    private UUID requestId;
    private List<Choice> choices;
    private boolean isPartial;
    private boolean isTruncated;

    @Getter
    @AllArgsConstructor
    public static class Usage {
        private int inputTokens;
        private int outputTokens;
        private int totalTokens;
        private double estimatedCosts;
    }

    @Getter
    @AllArgsConstructor
    public static class Choice {
        private FinishReason finishReason;
        private Message message;
    }

    @Getter
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



