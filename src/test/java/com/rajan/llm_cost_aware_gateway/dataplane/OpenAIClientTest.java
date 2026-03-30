package com.rajan.llm_cost_aware_gateway.dataplane;

import com.rajan.llm_cost_aware_gateway.dataplane.llm_clients.LLmClient;
import com.rajan.llm_cost_aware_gateway.dataplane.llm_clients.OpenAIClient;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLMRequest;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLMResponseChunk;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLmResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIClientTest {

    private LLmClient client;

    @BeforeEach
    void setup() {
        client = new OpenAIClient();
    }

    // ✅ 1. Null request
    @Test
    void complete_shouldHandleNullRequest() throws Exception {
        LLmResponse response = client.complete(null);

        assertNotNull(response);
        assertEquals(0, response.getUsage().getInputTokens());
        assertEquals(0, response.getUsage().getOutputTokens());
        assertEquals(0, response.getUsage().getTotalTokens());
        assertEquals(0.0, response.getUsage().getEstimatedCosts());
    }

    // ✅ 2. Null prompt
    @Test
    void complete_shouldHandleNullPrompt() throws Exception {
        LLMRequest request = LLMRequest.builder()
                .prompt(null)
                .build();

        LLmResponse response = client.complete(request);

        assertEquals(0, response.getUsage().getInputTokens());
        assertEquals(0, response.getUsage().getOutputTokens());
    }

    // ✅ 3. Token calculation correctness
    @Test
    void complete_shouldCalculateTokensCorrectly() throws Exception {
        LLMRequest request = LLMRequest.builder()
                .prompt("abcdefgh") // 8 chars → 2 tokens
                .maxTokens(10)
                .build();

        LLmResponse response = client.complete(request);

        assertEquals(2, response.getUsage().getInputTokens());
        assertTrue(response.getUsage().getOutputTokens() > 0);

        assertEquals(
                response.getUsage().getInputTokens() + response.getUsage().getOutputTokens(),
                response.getUsage().getTotalTokens()
        );
    }

    // ✅ 4. Validate response structure
    @Test
    void complete_shouldReturnValidResponseStructure() throws Exception {
        LLMRequest request = LLMRequest.builder()
                .prompt("test prompt")
                .maxTokens(20)
                .build();

        LLmResponse response = client.complete(request);

        assertNotNull(response.getModel());
        assertEquals("GPT-4", response.getModel());

        assertNotNull(response.getChoices());
        assertFalse(response.getChoices().isEmpty());

        LLmResponse.Choice choice = response.getChoices().get(0);
        assertEquals(LLmResponse.FinishReason.STOP, choice.getFinishReason());

        assertNotNull(choice.getMessage());
        assertNotNull(choice.getMessage().getContent());
        assertEquals("generic", choice.getMessage().getRole());
    }

    // ✅ 5. Cost sanity check
    @Test
    void complete_shouldHavePositiveCost() throws Exception {
        LLMRequest request = LLMRequest.builder()
                .prompt("some reasonably long prompt for cost")
                .maxTokens(50)
                .build();

        LLmResponse response = client.complete(request);

        assertTrue(response.getUsage().getEstimatedCosts() > 0);
    }

    // ✅ 6. Streaming basic behavior
    @Test
    void stream_shouldReturnChunks() {
        UUID requestId = UUID.randomUUID();

        LLMRequest request = LLMRequest.builder()
                .requestId(requestId)
                .prompt("abcdefgh")
                .maxTokens(10)
                .build();

        List<LLMResponseChunk> chunks = client.stream(request)
                .collect(Collectors.toList());

        assertFalse(chunks.isEmpty());

        // last chunk should be done
        assertTrue(chunks.get(chunks.size() - 1).isFinal());
    }

    // ✅ 7. Streaming token progression
    @Test
    void stream_shouldIncreaseOutputTokensSequentially() {
        UUID requestId = UUID.randomUUID();

        LLMRequest request = LLMRequest.builder()
                .requestId(requestId)
                .prompt("abcdefgh")
                .maxTokens(10)
                .build();

        List<LLMResponseChunk> chunks = client.stream(request)
                .collect(Collectors.toList());

        for (int i = 0; i < chunks.size(); i++) {
            assertEquals(i + 1, chunks.get(i).getUsageSoFar().getOutputTokens());
        }
    }

    // ✅ 8. Cancel streaming
    @Test
    void stream_shouldStopWhenCancelled() {
        UUID requestId = UUID.randomUUID();

        LLMRequest request = LLMRequest.builder()
                .requestId(requestId)
                .prompt("abcdefghabcdefghabcdefgh")
                .maxTokens(50)
                .build();

        var stream = client.stream(request);

        // cancel immediately
        client.cancel(requestId);

        List<LLMResponseChunk> chunks = stream.toList();

        // should stop early
        assertTrue(chunks.size() <50);
    }

    // ✅ 9. Input token edge case
    @Test
    void complete_shouldHandleSmallInputToken() throws Exception {
        LLMRequest request = LLMRequest.builder()
                .prompt("abc") // < 4 chars → 1 token
                .maxTokens(5)
                .build();

        LLmResponse response = client.complete(request);

        assertEquals(1, response.getUsage().getInputTokens());
    }
}