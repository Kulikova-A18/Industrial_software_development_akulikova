package com.cosmoscan.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Маршрут к File Storing Service
                .route("file-storing-service-route", r -> r
                        .path("/api/works/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("fileStoringServiceCB")
                                        .setFallbackUri("forward:/fallback/file-storing")))
                        .uri("http://localhost:8081"))
                
                // Маршрут к File Analysis Service
                .route("file-analysis-service-route", r -> r
                        .path("/api/reports/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("fileAnalysisServiceCB")
                                        .setFallbackUri("forward:/fallback/file-analysis")))
                        .uri("http://localhost:8082"))
                
                .build();
    }
}
