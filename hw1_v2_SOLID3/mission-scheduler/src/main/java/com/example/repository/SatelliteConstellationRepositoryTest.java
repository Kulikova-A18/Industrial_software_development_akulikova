// src/test/java/com/example/repository/SatelliteConstellationRepositoryTest.java
package com.example.repository;

import com.example.entity.SatelliteConstellation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.yml")
class SatelliteConstellationRepositoryTest {
    
    @Autowired
    private SatelliteConstellationRepository repository;
    
    @Test
    void shouldSaveAndFindConstellation() {
        SatelliteConstellation constellation = new SatelliteConstellation("Test-Constellation");
        constellation.setDescription("Test Description");
        
        SatelliteConstellation saved = repository.save(constellation);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test-Constellation");
    }
    
    @Test
    void shouldFindByName() {
        SatelliteConstellation constellation = new SatelliteConstellation("Unique-Name");
        repository.save(constellation);
        
        Optional<SatelliteConstellation> found = repository.findByName("Unique-Name");
        
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Unique-Name");
    }
    
    @Test
    void shouldCheckExistsByName() {
        SatelliteConstellation constellation = new SatelliteConstellation("Exists-Test");
        repository.save(constellation);
        
        boolean exists = repository.existsByName("Exists-Test");
        
        assertThat(exists).isTrue();
    }
    
    @Test
    void shouldDeleteConstellation() {
        SatelliteConstellation constellation = new SatelliteConstellation("To-Delete");
        SatelliteConstellation saved = repository.save(constellation);
        
        repository.deleteById(saved.getId());
        
        Optional<SatelliteConstellation> deleted = repository.findById(saved.getId());
        assertThat(deleted).isEmpty();
    }
}