package com.rajan.llm_cost_aware_gateway.utils;

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
}
