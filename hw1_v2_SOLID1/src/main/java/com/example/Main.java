package com.example;

import com.example.config.AppConfig;
import com.example.scheduler.ConfiguredMissionScheduler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    
    /**
     * Main entry point for the Mission Scheduler Service application.
     * Initializes Spring context, starts the mission scheduler, and registers
     * a shutdown hook for graceful application termination.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("MISSION PLANNER SERVICE RUNNING");
        System.out.println("Version: 1.0.0");
        System.out.println("Main service: http://localhost:8080/api ");
        System.out.println("Port: 8081 (for internal tasks only)");
        System.out.println();

        // Initialize Spring application context from configuration class
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        
        // Retrieve the mission scheduler bean and initialize it
        ConfiguredMissionScheduler scheduler = context.getBean(ConfiguredMissionScheduler.class);
        scheduler.init();
        
        // Register shutdown hook for graceful context closing on JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nSTOPPING THE SCHEDULER SERVICE");
            ((AnnotationConfigApplicationContext) context).close();
        }));
    }
}