package com.satellite.controller;

import com.satellite.dto.SatellitesOrderResponse;
import com.satellite.model.Satellite;
import com.satellite.service.SatelliteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/satellites")
@RequiredArgsConstructor
public class SatelliteController {

    private final SatelliteService satelliteService;

    @GetMapping
    public ResponseEntity<List<Satellite>> getAllSatellites() {
        log.debug("REST request to get all satellites");
        List<Satellite> satellites = satelliteService.getAllSatellites();
        return ResponseEntity.ok(satellites);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Satellite> getSatelliteById(@PathVariable Long id) {
        log.debug("REST request to get satellite by id: {}", id);
        return satelliteService.getSatelliteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Satellite> createSatellite(@Valid @RequestBody Satellite satellite) {
        log.info("REST request to create satellite: {}", satellite.getName());
        Satellite created = satelliteService.createSatellite(satellite);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Satellite> updateSatellite(@PathVariable Long id, 
                                                    @Valid @RequestBody Satellite satellite) {
        log.info("REST request to update satellite id: {}", id);
        try {
            Satellite updated = satelliteService.updateSatellite(id, satellite);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSatellite(@PathVariable Long id) {
        log.info("REST request to delete satellite id: {}", id);
        if (satelliteService.deleteSatellite(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/check-order")
    public ResponseEntity<SatellitesOrderResponse> checkOrder(
            @RequestParam(defaultValue = "altitude") String sortBy) {
        log.info("REST request to check satellite order by: {}", sortBy);
        SatellitesOrderResponse response = satelliteService.checkSatellitesOrder(sortBy);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Satellite Service is running");
    }
}