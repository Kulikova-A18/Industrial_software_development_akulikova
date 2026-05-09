// com/example/repository/CommunicationSatelliteRepository.java
package com.example.repository;

import com.example.entity.CommunicationSatellite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommunicationSatelliteRepository extends JpaRepository<CommunicationSatellite, Long> {
    List<CommunicationSatellite> findByTransponderCountGreaterThan(Integer minTransponders);
}