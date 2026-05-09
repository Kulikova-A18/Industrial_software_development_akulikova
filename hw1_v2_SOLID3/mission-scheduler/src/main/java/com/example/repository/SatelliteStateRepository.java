// com/example/repository/SatelliteStateRepository.java
package com.example.repository;

import com.example.entity.SatelliteState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SatelliteStateRepository extends JpaRepository<SatelliteState, Long> {
    Optional<SatelliteState> findBySatelliteId(Long satelliteId);
    List<SatelliteState> findByIsActiveTrue();
    List<SatelliteState> findByBatteryLevelLessThan(Double threshold);
    
    @Modifying
    @Transactional
    @Query("UPDATE SatelliteState s SET s.batteryLevel = :batteryLevel, s.lastUpdateTime = :updateTime WHERE s.satellite.id = :satelliteId")
    int updateBatteryLevel(@Param("satelliteId") Long satelliteId, @Param("batteryLevel") Double batteryLevel, @Param("updateTime") LocalDateTime updateTime);
}