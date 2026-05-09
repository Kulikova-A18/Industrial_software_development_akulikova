// com/example/repository/SatelliteConstellationRepository.java
package com.example.repository;

import com.example.entity.SatelliteConstellation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SatelliteConstellationRepository extends JpaRepository<SatelliteConstellation, Long> {
    Optional<SatelliteConstellation> findByName(String name);
    boolean existsByName(String name);
}