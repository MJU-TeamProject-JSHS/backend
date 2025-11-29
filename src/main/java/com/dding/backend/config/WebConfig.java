package com.dding.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    //@Override
    public void addCorsMapping(CorsRegistry registry) {
        registry.addMapping("api/genai/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST")
                .allowCredentials(true);
    }
}
