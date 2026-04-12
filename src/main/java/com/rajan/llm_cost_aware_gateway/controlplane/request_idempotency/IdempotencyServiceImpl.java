package com.rajan.llm_cost_aware_gateway.controlplane.request_idempotency;

import com.rajan.llm_cost_aware_gateway.controlplane.models.IdempotencyResult;
import com.rajan.llm_cost_aware_gateway.controlplane.models.Request;
import com.rajan.llm_cost_aware_gateway.controlplane.models.Response;
import com.rajan.llm_cost_aware_gateway.exceptions.RetryLaterException;
import com.rajan.llm_cost_aware_gateway.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.rajan.llm_cost_aware_gateway.constants.CommonConstants.*;

@Slf4j
@Service
public class IdempotencyServiceImpl implements IdempotencyService {
    private final RedissonClient redisClient;

    @Autowired
    public IdempotencyServiceImpl(RedissonClient redissonClient) {
        this.redisClient = redissonClient;
    }

    @Override
    public Optional<IdempotencyResult> preHandle(Request request) {
        log.info("IdempotencyServiceImpl preHandle for request: {}", request);
        final RMapCache<String, IdempotencyResult> idempotencyResultMap = redisClient.getMapCache(REQUEST_IDEMPOTENCY_KEY_PREFIX);
        final var key = Utils.constructKey(request.getOrgId(), request.getIdempotencyKey().toString());
        final var result = idempotencyResultMap.get(key);
        final var requestHash = Utils.getRequestHash(request);
        if (result == null) {
            idempotencyResultMap.put(key, new IdempotencyResult(IdempotencyResult.Status.IN_PROGRESS, requestHash, null, 0L, Instant.now().getEpochSecond()), 5, TimeUnit.MINUTES);
            return Optional.empty();
        } else {
            if (result.status() == IdempotencyResult.Status.IN_PROGRESS) {
                throw new RetryLaterException();
            }
            if (!result.requestHash().equals(requestHash)) {
                throw new IllegalArgumentException("RequestHash mismatch. Use a unique idempotency key for a new request");
            }
            return Optional.of(result);
        }
    }

    @Override
    public void onSuccess(Request request, Response response, long tokensUsed) {
        log.info("IdempotencyServiceImpl onSuccess for request: {}", request);
        final RMapCache<String, IdempotencyResult> idempotencyResultMap = redisClient.getMapCache(REQUEST_IDEMPOTENCY_KEY_PREFIX);
        final var key = Utils.constructKey(request.getOrgId(), request.getIdempotencyKey().toString());
        final var requestHash = Utils.getRequestHash(request);
        idempotencyResultMap.put(key, new IdempotencyResult(IdempotencyResult.Status.COMPLETED, requestHash, response, tokensUsed, Instant.now().getEpochSecond()), 60, TimeUnit.MINUTES);
    }

    @Override
    public void onFailure(Request request) {
        log.info("IdempotencyServiceImpl onFailure for request: {}", request);
        final RMapCache<String, IdempotencyResult> idempotencyResultMap = redisClient.getMapCache(REQUEST_IDEMPOTENCY_KEY_PREFIX);
        final var key = Utils.constructKey(request.getOrgId(), request.getIdempotencyKey().toString());
        idempotencyResultMap.remove(key);
    }
}
