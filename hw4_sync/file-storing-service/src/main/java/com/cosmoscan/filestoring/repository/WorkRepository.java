package com.cosmoscan.filestoring.repository;

import com.cosmoscan.filestoring.entity.WorkSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for {@link WorkSubmission} entities
 * 
 * @see JpaRepository
 * @see WorkSubmission
 */
public interface WorkRepository extends JpaRepository<WorkSubmission, UUID> {
}