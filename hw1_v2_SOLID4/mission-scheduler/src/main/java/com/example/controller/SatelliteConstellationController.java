// com/example/controller/SatelliteConstellationController.java
package com.example.controller;

import com.example.entity.SatelliteConstellation;
import com.example.repository.SatelliteConstellationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/constellations")
public class SatelliteConstellationController {
    
    private final SatelliteConstellationRepository repository;
    
    public SatelliteConstellationController(SatelliteConstellationRepository repository) {
        this.repository = repository;
    }
    
    @GetMapping
    public List<SatelliteConstellation> getAll() {
        return repository.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SatelliteConstellation> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<SatelliteConstellation> getByName(@RequestParam String name) {
        return repository.findByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<SatelliteConstellation> create(@RequestBody SatelliteConstellation constellation) {
        if (repository.existsByName(constellation.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(constellation));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SatelliteConstellation> update(@PathVariable Long id, @RequestBody SatelliteConstellation constellation) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        constellation.setId(id);
        return ResponseEntity.ok(repository.save(constellation));
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