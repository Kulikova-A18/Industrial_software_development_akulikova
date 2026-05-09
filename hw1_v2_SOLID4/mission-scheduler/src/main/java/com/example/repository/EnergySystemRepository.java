// com/example/repository/EnergySystemRepository.java
package com.example.repository;

import com.example.entity.EnergySystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EnergySystemRepository extends JpaRepository<EnergySystem, Long> {
    Optional<EnergySystem> findBySatelliteId(Long satelliteId);
}