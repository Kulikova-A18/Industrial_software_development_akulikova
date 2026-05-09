package com.example.config;

import com.example.client.SpaceOperationClient;
import com.example.properties.MissionProperties;
import com.example.scheduler.ConfiguredMissionScheduler;
import com.example.service.MissionExecutionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ComponentScan(basePackages = "com.example")
public class AppConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            String port = System.getenv().getOrDefault("SERVER_PORT", "8081");
            factory.setPort(Integer.parseInt(port));
        };
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper(new YAMLFactory());
    }

    @Bean
    public MissionProperties missionProperties(ObjectMapper objectMapper) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.yml");
        if (inputStream == null) {
            Path configPath = Paths.get("src/main/resources/application.yml");
            inputStream = new FileInputStream(configPath.toFile());
        }
        return objectMapper.readValue(inputStream, MissionProperties.class);
    }

    @Bean
    public RestClient restClient() {
        String baseUrl = System.getenv().getOrDefault("SERVER_URL", "http://localhost:8080/api");
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public SpaceOperationClient spaceOperationClient(RestClient restClient) {
        return new SpaceOperationClient(restClient);
    }

    @Bean
    public MissionExecutionService missionExecutionService(SpaceOperationClient client) {
        return new MissionExecutionService(client);
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("mission-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    public ConfiguredMissionScheduler configuredMissionScheduler(
            MissionProperties properties,
            ThreadPoolTaskScheduler taskScheduler,
            MissionExecutionService missionExecutionService) {
        return new ConfiguredMissionScheduler(properties, taskScheduler, missionExecutionService);
    }
}