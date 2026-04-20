package com.rajan.llm_cost_aware_gateway.dataplane.llm_clients;

import com.rajan.llm_cost_aware_gateway.dataplane.contstants.Constants;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLMRequest;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLMResponseChunk;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLmResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static com.rajan.llm_cost_aware_gateway.dataplane.contstants.Constants.MILLION;

@Slf4j
@Service(value = "GEMINI")
public class GeminiClient implements LLmClient {

    // Gemini 1.5 Flash style pricing (Example: $0.075 per 1M input, $0.30 per 1M output)
    private final double inputCostPerMillion = 0.075;
    private final double outputCostPerMillion = 0.30;

    private final ConcurrentHashMap<UUID, AtomicBoolean> cancelMap = new ConcurrentHashMap<>();

    @Override
    public LLmResponse complete(LLMRequest request) throws InterruptedException {
        log.info("GeminiClient processing unary request: {}", request.getRequestId());

        if (request == null || request.getPrompt() == null) {
            return LLmResponse.builder()
                    .usage(new LLmResponse.Usage(0, 0, 0, 0.0))
                    .model("gemini-1.5-flash")
                    .build();
        }

        // Simulate network latency
        Thread.sleep(ThreadLocalRandom.current().nextInt(500, 3000));

        int inputTokens = countTokens(request.getPrompt());
        String generatedText = generateFakeResponse(request.getMaxTokens());
        int outputTokens = countTokens(generatedText);
        double cost = calculateCost(inputTokens, outputTokens);

        return LLmResponse.builder()
                .requestId(request.getRequestId())
                .model("gemini-1.5-flash")
                .usage(new LLmResponse.Usage(inputTokens, outputTokens, inputTokens + outputTokens, cost))
                .choices(List.of(new LLmResponse.Choice(
                        LLmResponse.FinishReason.STOP,
                        new LLmResponse.Message("model", generatedText))))
                .build();
    }

    @Override
    public Stream<LLMResponseChunk> stream(LLMRequest request) {
        log.info("GeminiClient starting stream for request: {}", request.getRequestId());

        if (request == null || request.getPrompt() == null) return Stream.empty();

        final int inputTokens = countTokens(request.getPrompt());
        final List<String> tokenChunks = tokenize(generateFakeResponse(request.getMaxTokens()));
        final AtomicLong currentTokenIndex = new AtomicLong(0);

        // Ensure entry in cancel map
        cancelMap.putIfAbsent(request.getRequestId(), new AtomicBoolean(false));

        return tokenChunks.stream()
                .takeWhile(chunk -> {
                    boolean isCancelled = cancelMap.getOrDefault(request.getRequestId(), new AtomicBoolean(false)).get();
                    if (isCancelled) {
                        cleanup(request.getRequestId());
                        return false;
                    }
                    return true;
                })
                .map(chunk -> {
                    try {
                        // Simulate "thinking" time per token
                        Thread.sleep(ThreadLocalRandom.current().nextInt(20, 100));

                        long index = currentTokenIndex.incrementAndGet();
                        boolean isLast = (index == tokenChunks.size());

                        if (isLast) cleanup(request.getRequestId());

                        return new LLMResponseChunk(
                                request.getRequestId(),
                                chunk,
                                isLast,
                                new LLmResponse.Usage(
                                        inputTokens,
                                        (int) index,
                                        (int) (inputTokens + index),
                                        calculateCost(inputTokens, index))
                        );
                    } catch (InterruptedException e) {
                        cleanup(request.getRequestId());
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Stream interrupted", e);
                    }
                })
                .onClose(() -> cleanup(request.getRequestId()));
    }

    @Override
    public void cancel(UUID requestId) {
        log.info("Cancelling request: {}", requestId);
        if (cancelMap.containsKey(requestId)) {
            cancelMap.get(requestId).set(true);
        }
    }

    private void cleanup(UUID requestId) {
        cancelMap.remove(requestId);
    }

    private int countTokens(String text) {
        return (text == null) ? 0 : (int) Math.ceil(text.length() / 4.0);
    }

    private List<String> tokenize(String text) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += 4) {
            chunks.add(text.substring(i, Math.min(i + 4, text.length())));
        }
        return chunks;
    }

    private double calculateCost(long input, long output) {
        return (input / MILLION) * inputCostPerMillion + (output / MILLION) * outputCostPerMillion;
    }

    private String generateFakeResponse(long maxTokens) {
        // Randomly decide response length relative to maxTokens
        double factor = ThreadLocalRandom.current().nextBoolean() ? 0.4 : 0.9;
        int targetLength = (int) (maxTokens * 4 * factor);

        return Constants.RESPONSE_TEXT.substring(0,
                Math.min(targetLength, Constants.RESPONSE_TEXT.length()));
    }
}