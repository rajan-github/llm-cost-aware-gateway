package com.rajan.llm_cost_aware_gateway.cache;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryCache implements Cache {
    private final Map<String, Object> map;

    public InMemoryCache() {
        this.map = new ConcurrentHashMap<>();
    }


    @Override
    public void setValue(String key, Object value) {
        map.put(key, value);
    }

    @Override
    public Optional<Object> getValue(String key) {
        if (map.containsKey(key)) {
            return Optional.of(map.get(key));
        } else {
            return Optional.empty();
        }
    }
}
