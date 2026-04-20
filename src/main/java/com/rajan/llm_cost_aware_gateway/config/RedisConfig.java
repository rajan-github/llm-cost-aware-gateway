package com.rajan.llm_cost_aware_gateway.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJackson3Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.codec.Kryo5Codec;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    @Bean(destroyMethod = "shutdown")
    public static RedissonClient createClient() {
        Config config = new Config();
        config.setCodec(new Kryo5Codec());
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setConnectionMinimumIdleSize(5)
                .setConnectionPoolSize(10);

        return Redisson.create(config);
    }
}
