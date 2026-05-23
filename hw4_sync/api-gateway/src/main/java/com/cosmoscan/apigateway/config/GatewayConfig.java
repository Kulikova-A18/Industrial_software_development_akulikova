package com.cosmoscan.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for defining custom API Gateway routes using Spring Cloud Gateway.
 * 
 * <p>This configuration sets up the routing rules that direct incoming HTTP requests
 * to the appropriate downstream microservices. It also integrates circuit breaker
 * patterns to provide fault tolerance and graceful degradation when services are
 * unavailable.</p>
 * 
 * <p>The gateway acts as a single entry point for the Cosmoscan application,
 * routing requests to:
 * <ul>
 *   <li>File Storing Service - handles workspace-related operations</li>
 *   <li>File Analysis Service - handles report generation and analysis</li>
 * </ul>
 * </p>
 *
 * @see org.springframework.cloud.gateway.route.RouteLocator
 * @see org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
 */
@Configuration
public class GatewayConfig {
    
    /**
     * Defines the custom route locator bean that configures all API gateway routes.
     * 
     * <p>This method creates and returns a {@link RouteLocator} instance using the
     * provided {@link RouteLocatorBuilder}. Each route is configured with path-based
     * routing predicates and circuit breaker filters to enhance system resilience.</p>
     * 
     * <p><strong>Circuit Breaker Configuration:</strong> Each route includes a
     * circuit breaker filter that monitors service health. If a downstream service
     * fails repeatedly, the circuit breaker opens and redirects requests to a
     * fallback endpoint, preventing cascading failures across the system.</p>
     *
     * @param builder the {@link RouteLocatorBuilder} injected by Spring, used to
     *                construct route definitions in a fluent, declarative manner.
     *                Provides methods for defining routes, predicates, filters,
     *                and destination URIs. Must not be {@code null}.
     * @return a fully configured {@link RouteLocator} containing all defined
     *         routes with their associated predicates, filters, and target URIs.
     *         Never returns {@code null}.
     * 
     * @see RouteLocatorBuilder#routes()
     * @see org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerFilterFactory
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                /**
                 * Route definition for the File Storing Service.
                 * 
                 * Predicate: Matches any request with a path starting with "/api/works/"
                 *            (e.g., /api/works/create, /api/works/{id}, /api/works/{id}/delete).
                 * 
                 * Filter: Circuit breaker named "fileStoringServiceCB" that monitors
                 *         the health of the File Storing Service. When the circuit
                 *         is open (service unavailable or failing), requests are
                 *         forwarded to "/fallback/file-storing" for graceful degradation.
                 * 
                 * URI: Routes matching requests to the File Storing Service running at
                 *      http://localhost:8081. In production, this should be replaced
                 *      with the service's actual hostname or load balancer address.
                 */
                .route("file-storing-service-route", r -> r
                        .path("/api/works/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("fileStoringServiceCB")
                                        .setFallbackUri("forward:/fallback/file-storing")))
                        .uri("http://localhost:8081"))
                
                /**
                 * Route definition for the File Analysis Service.
                 * 
                 * Predicate: Matches any request with a path starting with "/api/reports/"
                 *            (e.g., /api/reports/generate, /api/reports/{id}/download,
                 *            /api/reports/{id}/status).
                 * 
                 * Filter: Circuit breaker named "fileAnalysisServiceCB" that monitors
                 *         the health of the File Analysis Service. When the circuit
                 *         is open (service unavailable or failing), requests are
                 *         forwarded to "/fallback/file-analysis" for graceful degradation.
                 * 
                 * URI: Routes matching requests to the File Analysis Service running at
                 *      http://localhost:8082. In production, this should be replaced
                 *      with the service's actual hostname or load balancer address.
                 */
                .route("file-analysis-service-route", r -> r
                        .path("/api/reports/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("fileAnalysisServiceCB")
                                        .setFallbackUri("forward:/fallback/file-analysis")))
                        .uri("http://localhost:8082"))
                
                /*
                 * Builds and returns the complete RouteLocator with all defined routes.
                 * The build() method finalizes the route configuration and creates
                 * the immutable route definitions that will be used by the gateway.
                 */
                .build();
    }
}