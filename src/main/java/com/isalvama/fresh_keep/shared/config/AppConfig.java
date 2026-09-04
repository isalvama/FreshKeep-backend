package com.isalvama.fresh_keep.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import java.time.Clock;

@Configuration
@EnableRetry
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
