package com.cosmoscan.filestoring.repository;

import com.cosmoscan.filestoring.entity.WorkSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface WorkRepository extends JpaRepository<WorkSubmission, UUID> {
}
