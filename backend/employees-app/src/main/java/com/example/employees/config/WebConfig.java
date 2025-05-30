package com.example.employees.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String frontendUrl;

    public WebConfig(@Value("${frontend.url}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // or "/**" to allow everything
                .allowedOrigins(frontendUrl) // Vite dev server
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
