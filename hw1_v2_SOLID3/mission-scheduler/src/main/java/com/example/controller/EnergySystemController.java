// com/example/controller/EnergySystemController.java
package com.example.controller;

import com.example.entity.EnergySystem;
import com.example.repository.EnergySystemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/energy-systems")
public class EnergySystemController {
    
    private final EnergySystemRepository repository;
    
    public EnergySystemController(EnergySystemRepository repository) {
        this.repository = repository;
    }
    
    @GetMapping
    public List<EnergySystem> getAll() {
        return repository.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EnergySystem> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/satellite/{satelliteId}")
    public ResponseEntity<EnergySystem> getBySatelliteId(@PathVariable Long satelliteId) {
        return repository.findBySatelliteId(satelliteId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<EnergySystem> create(@RequestBody EnergySystem energySystem) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(energySystem));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EnergySystem> update(@PathVariable Long id, @RequestBody EnergySystem energySystem) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        energySystem.setId(id);
        return ResponseEntity.ok(repository.save(energySystem));
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