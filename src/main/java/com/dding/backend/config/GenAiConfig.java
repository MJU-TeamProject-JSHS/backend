package com.dding.backend.config;

import com.google.genai.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenAiConfig {
    @Bean
    public Client genAiClient() {
        return new Client();
    }
}
