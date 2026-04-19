package com.rajan.llm_cost_aware_gateway.utils;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.rajan.llm_cost_aware_gateway.constants.CommonConstants;
import com.rajan.llm_cost_aware_gateway.controlplane.models.Request;

import java.nio.charset.StandardCharsets;

public class Utils {
    public static long getTokens(String str) {
        if (str == null) {
            return 0;
        }
        long tokens = 0;
        for (int i = 0; i < str.length(); i += 4) {
            tokens += 1;
        }
        return tokens;
    }

    public static String createLedgerKey(String orgId, String endpoint, String model) {
        if (orgId == null || endpoint == null || model == null) {
            throw new IllegalArgumentException("Ledger keys cannot be null");
        }
        return orgId + CommonConstants.KEY_SEPARATOR + endpoint + CommonConstants.KEY_SEPARATOR + model;
    }

    public static String constructKey(String... args) {
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("Keys cannot be null");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(args[0]);
        for (int i = 1; i < args.length; i++) {
            sb.append(CommonConstants.KEY_SEPARATOR);
            sb.append(args[i]);
        }
        return sb.toString();
    }

    public static String getRequestHash(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        final Hasher hasher = Hashing.murmur3_128().newHasher();
        hasher.putString(request.getOrgId(), StandardCharsets.UTF_8);
        hasher.putString(request.getPrompt(), StandardCharsets.UTF_8);
        hasher.putString(request.getModel(), StandardCharsets.UTF_8);
        hasher.putString(request.getEndpoint().name(), StandardCharsets.UTF_8);
        return hasher.hash().toString();
    }
}
