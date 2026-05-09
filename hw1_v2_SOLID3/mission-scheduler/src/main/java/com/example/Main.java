package com.example;

import com.example.config.AppConfig;
import com.example.config.AspectConfig;
import com.example.scheduler.ConfiguredMissionScheduler;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Main {
    
    public static void main(String[] args) {
        System.out.println("MISSION PLANNER SERVICE RUNNING");
        System.out.println("Version: 1.0.0");
        System.out.println("Main service: " + 
            System.getenv().getOrDefault("SERVER_URL", "http://localhost:8080/api"));
        System.out.println("Port: " + 
            System.getenv().getOrDefault("SERVER_PORT", "8081"));
        System.out.println();

        ConfigurableApplicationContext context = new SpringApplicationBuilder(
                AppConfig.class, 
                AspectConfig.class)
                .run(args);
        
        ConfiguredMissionScheduler scheduler = context.getBean(ConfiguredMissionScheduler.class);
        scheduler.init();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nSTOPPING THE SCHEDULER SERVICE");
            context.close();
        }));
    }
}