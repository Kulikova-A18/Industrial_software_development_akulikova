// com/example/repository/SatelliteRepository.java
package com.example.repository;

import com.example.entity.Satellite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SatelliteRepository extends JpaRepository<Satellite, Long> {
    Optional<Satellite> findByName(String name);
    List<Satellite> findByConstellationId(Long constellationId);
    List<Satellite> findByOperational(boolean operational);
    
    @Query("SELECT s FROM Satellite s WHERE s.constellation.name = :constellationName")
    List<Satellite> findByConstellationName(@Param("constellationName") String constellationName);
}