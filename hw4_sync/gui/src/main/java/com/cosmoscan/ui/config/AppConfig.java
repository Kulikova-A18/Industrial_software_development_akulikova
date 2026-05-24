package com.cosmoscan.ui.config;

import com.cosmoscan.ui.service.ApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    
    @Value("${api.gateway.url}")
    private String gatewayUrl;
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public ApiService apiService(RestTemplate restTemplate) {
        return new ApiService(gatewayUrl, restTemplate);
    }
}
