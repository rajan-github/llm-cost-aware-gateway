package com.rajan.llm_cost_aware_gateway.utils;

import com.rajan.llm_cost_aware_gateway.constants.CommonConstants;
import com.rajan.llm_cost_aware_gateway.dataplane.contstants.Constants;

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
}
