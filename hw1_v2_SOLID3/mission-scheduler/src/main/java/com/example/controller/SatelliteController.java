// com/example/controller/SatelliteController.java
package com.example.controller;

import com.example.entity.Satellite;
import com.example.repository.SatelliteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/satellites")
public class SatelliteController {
    
    private final SatelliteRepository repository;
    
    public SatelliteController(SatelliteRepository repository) {
        this.repository = repository;
    }
    
    @GetMapping
    public List<Satellite> getAll() {
        return repository.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Satellite> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/by-constellation/{constellationId}")
    public List<Satellite> getByConstellation(@PathVariable Long constellationId) {
        return repository.findByConstellationId(constellationId);
    }
    
    @GetMapping("/operational")
    public List<Satellite> getOperational() {
        return repository.findByOperational(true);
    }
    
    @PostMapping
    public ResponseEntity<Satellite> create(@RequestBody Satellite satellite) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(satellite));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Satellite> update(@PathVariable Long id, @RequestBody Satellite satellite) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        satellite.setId(id);
        return ResponseEntity.ok(repository.save(satellite));
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