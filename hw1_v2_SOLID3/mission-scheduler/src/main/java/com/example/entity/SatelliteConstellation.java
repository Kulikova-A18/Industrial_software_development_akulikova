// com/example/entity/SatelliteConstellation.java
package com.example.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "satellite_constellations", indexes = {
    @Index(name = "idx_constellation_name", columnList = "name")
})
public class SatelliteConstellation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @OneToMany(mappedBy = "constellation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Satellite> satellites = new ArrayList<>();
    
    public SatelliteConstellation() {}
    
    public SatelliteConstellation(String name) {
        this.name = name;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Satellite> getSatellites() { return satellites; }
    public void setSatellites(List<Satellite> satellites) { this.satellites = satellites; }
}