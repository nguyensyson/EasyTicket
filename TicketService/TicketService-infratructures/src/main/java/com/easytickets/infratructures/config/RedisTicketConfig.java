package com.easytickets.infratructures.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * Loads the CHECK & DECREMENT Lua script used by {@code TicketInventoryRepositoryImpl}
 * to atomically reserve stock on Redis (the single source of truth for ticket inventory).
 */
@Configuration
public class RedisTicketConfig {

    @Bean
    public DefaultRedisScript<Long> checkAndDecrementScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/check_and_decrement.lua"));
        script.setResultType(Long.class);
        return script;
    }
}
