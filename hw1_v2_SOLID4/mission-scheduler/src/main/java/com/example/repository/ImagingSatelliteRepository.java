// com/example/repository/ImagingSatelliteRepository.java
package com.example.repository;

import com.example.entity.ImagingSatellite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImagingSatelliteRepository extends JpaRepository<ImagingSatellite, Long> {
    List<ImagingSatellite> findByResolutionGreaterThan(Double minResolution);
}