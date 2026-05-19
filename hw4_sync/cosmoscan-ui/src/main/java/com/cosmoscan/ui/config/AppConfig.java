package com.cosmoscan.ui.config;

import com.cosmoscan.ui.service.ApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {
    @Value("${api.gateway.url}") private String gatewayUrl;
    @Value("${api.timeout}") private int apiTimeout;

    @Bean
    public ApiService apiService() {
        return new ApiService(gatewayUrl, apiTimeout);
    }
}
