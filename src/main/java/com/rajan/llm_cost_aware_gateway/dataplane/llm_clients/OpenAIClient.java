package com.rajan.llm_cost_aware_gateway.dataplane.llm_clients;

import com.rajan.llm_cost_aware_gateway.dataplane.contstants.Constants;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLMRequest;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLMResponseChunk;
import com.rajan.llm_cost_aware_gateway.dataplane.models.LLmResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static com.rajan.llm_cost_aware_gateway.dataplane.contstants.Constants.MILLION;

@Slf4j
@Service
public class OpenAIClient implements LLmClient {
    private final double inputCostPerMillion = 0.15;
    private final double outputCostPerMillion = 5.0;

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
            final String modelResponse = fakeResponse(request.getPrompt(), request.getMaxTokens());
            var inputTokens = request.getPrompt().split(" ").length;
            var outputTokens = modelResponse.split(" ").length;


            double estimatedCost =
                    (inputTokens / MILLION) * inputCostPerMillion +
                            (outputTokens / MILLION) * outputCostPerMillion;
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

    @Override
    public Stream<LLMResponseChunk> stream(LLMRequest request) {
        return Stream.empty();
    }

    @Override
    public void cancel(UUID requestId) {

    }

    private String fakeResponse(String prompt, int maxTokens) {
        int length = ThreadLocalRandom.current().nextInt(prompt.length(), Math.min(maxTokens, 3000));
        return Constants.RESPONSE_TEXT.substring(0, length);
    }
}
