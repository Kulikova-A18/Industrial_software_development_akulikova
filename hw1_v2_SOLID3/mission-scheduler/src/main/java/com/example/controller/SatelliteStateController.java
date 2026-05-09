// com/example/controller/SatelliteStateController.java
package com.example.controller;

import com.example.entity.SatelliteState;
import com.example.repository.SatelliteStateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/satellite-states")
public class SatelliteStateController {
    
    private final SatelliteStateRepository repository;
    
    public SatelliteStateController(SatelliteStateRepository repository) {
        this.repository = repository;
    }
    
    @GetMapping
    public List<SatelliteState> getAll() {
        return repository.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SatelliteState> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/satellite/{satelliteId}")
    public ResponseEntity<SatelliteState> getBySatelliteId(@PathVariable Long satelliteId) {
        return repository.findBySatelliteId(satelliteId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/active")
    public List<SatelliteState> getActive() {
        return repository.findByIsActiveTrue();
    }
    
    @GetMapping("/low-battery")
    public List<SatelliteState> getLowBattery(@RequestParam(defaultValue = "20.0") Double threshold) {
        return repository.findByBatteryLevelLessThan(threshold);
    }
    
    @PostMapping
    public ResponseEntity<SatelliteState> create(@RequestBody SatelliteState state) {
        state.setLastUpdateTime(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(state));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SatelliteState> update(@PathVariable Long id, @RequestBody SatelliteState state) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        state.setId(id);
        state.setLastUpdateTime(LocalDateTime.now());
        return ResponseEntity.ok(repository.save(state));
    }
    
    @PatchMapping("/{satelliteId}/battery")
    public ResponseEntity<Void> updateBattery(@PathVariable Long satelliteId, @RequestParam Double batteryLevel) {
        int updated = repository.updateBatteryLevel(satelliteId, batteryLevel, LocalDateTime.now());
        return updated > 0 ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}