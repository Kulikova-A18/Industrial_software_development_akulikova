package com.satellite.utils;

import com.satellite.models.Satellite;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
public class TestDataGenerator {
    
    private static final Random random = new Random();
    
    public static Satellite generateRandomSatellite() {
        Satellite satellite = new Satellite();
        satellite.setName("Test-Sat-" + System.currentTimeMillis());
        satellite.setAltitude(300 + random.nextDouble() * 2000);
        satellite.setInclination(20 + random.nextDouble() * 70);
        satellite.setSpeed(5 + random.nextDouble() * 5);
        satellite.setLaunchDate(LocalDateTime.now().minusDays(random.nextInt(365)));
        satellite.setStatus("ACTIVE");
        
        log.debug("Generated random satellite {}", satellite);
        return satellite;
    }
    
    public static Satellite generateTestSatellite(String name) {
        Satellite satellite = new Satellite();
        satellite.setName(name);
        satellite.setAltitude(500.0);
        satellite.setInclination(45.0);
        satellite.setSpeed(7.5);
        satellite.setLaunchDate(LocalDateTime.now());
        satellite.setStatus("ACTIVE");
        
        log.debug("Generated test satellite {}", satellite);
        return satellite;
    }
}