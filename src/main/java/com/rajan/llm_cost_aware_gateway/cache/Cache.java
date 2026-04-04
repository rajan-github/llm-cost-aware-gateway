package com.rajan.llm_cost_aware_gateway.cache;

import java.util.Optional;

public interface Cache {
    void setValue(String key, Object value);

    Optional<Object> getValue(String key);
}
