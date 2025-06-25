package com.clinicanuevomilenio.equipamientos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient pabellonWebClient(WebClient.Builder builder) {
        // Apunta a la URL base de tu pabellones-api (puerto 8082)
        return builder.baseUrl("http://localhost:8003/api/pabellones").build();
    }
}