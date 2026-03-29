package com.rajan.llm_cost_aware_gateway.dataplane.llm_clients;

import com.rajan.llm_cost_aware_gateway.dataplane.contstants.Constants;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLMRequest;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLMResponseChunk;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLmResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static com.rajan.llm_cost_aware_gateway.dataplane.contstants.Constants.MILLION;

@Slf4j
@Service
public class OpenAIClient implements LLmClient {
    private final double inputCostPerMillion = 0.15;
    private final double outputCostPerMillion = 5.0;
    private ConcurrentHashMap<UUID, AtomicBoolean> cancelMap = new ConcurrentHashMap<>();

    @Override
    public LLmResponse complete(LLMRequest request) throws InterruptedException {
        log.info("OpenAIClient request received with payload: {}", request);
        if (request == null || request.getPrompt() == null) {
            var response = LLmResponse.builder().
                    usage(new LLmResponse.Usage(0, 0, 0, 0))
                    .model("GPT-4")
                    .build();
            return response;
        }
        int delay = ThreadLocalRandom.current().nextInt(0, 10000);
        try {
            Thread.sleep(delay);
            var inputTokens = request.getPrompt().split(" ").length;
            final String modelResponse = fakeResponse(request.getMaxTokens());
            var outputTokens = modelResponse.split(" ").length;

            double estimatedCost = getEstimatedCost(inputTokens, outputTokens);

            var requestResponse = LLmResponse.builder()
                    .usage(new LLmResponse.Usage(inputTokens, outputTokens, inputTokens + outputTokens, estimatedCost))
                    .model("GPT-4")
                    .choices(List.of(new LLmResponse.Choice(LLmResponse.FinishReason.STOP, new LLmResponse.Message("generic", modelResponse))))
                    .build();
            return requestResponse;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    private double getEstimatedCost(long inputTokens, long outputTokens) {
        return (inputTokens / MILLION) * inputCostPerMillion +
                (outputTokens / MILLION) * outputCostPerMillion;
    }

    @Override
    public Stream<LLMResponseChunk> stream(LLMRequest request) {
        log.info("OpenAIClient request received for streaming with payload: {}", request);
        if (request == null || request.getPrompt() == null) {
            return Stream.empty();
        }
        final AtomicLong tokenCount = new AtomicLong(0);
        final String response = fakeResponse(request.getMaxTokens());
        final int inputTokens = request.getPrompt().split(" ").length;
        return Arrays.stream(response.split(" ")).map(token -> {
            if (cancelMap.containsKey(request.getRequestId())) {
                return new LLMResponseChunk(
                        request.getRequestId(), token, true, new LLmResponse.Usage(inputTokens, (int) tokenCount.get(), (int) tokenCount.get() + inputTokens, getEstimatedCost(inputTokens, tokenCount.get()))
                );
            }
            try {
                Thread.sleep(50);
                long current = tokenCount.incrementAndGet();
                return new LLMResponseChunk(
                        request.getRequestId(), token, false, new LLmResponse.Usage(inputTokens, (int) current, (int) current + inputTokens, getEstimatedCost(inputTokens, current))
                );
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void cancel(UUID requestId) {
        this.cancelMap.put(requestId, new AtomicBoolean(true));
    }

    private String fakeResponse(int maxTokens) {
        double p = ThreadLocalRandom.current().nextDouble();
        int outputTokens;
        if (p < 0.7) {
            outputTokens = (int) (0.5 * maxTokens);
        } else if (p < 0.9) {
            outputTokens = (int) (0.8 * maxTokens);
        } else {
            outputTokens = maxTokens;
        }
        int charLength = outputTokens << 2;
        return Constants.RESPONSE_TEXT.substring(0, Math.min(charLength, Constants.RESPONSE_TEXT.length()));
    }
}
