// com/example/entity/Satellite.java
package com.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "satellites", indexes = {
    @Index(name = "idx_satellite_name", columnList = "name"),
    @Index(name = "idx_satellite_constellation", columnList = "constellation_id")
})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "satellite_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Satellite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "constellation_id", nullable = false)
    private SatelliteConstellation constellation;
    
    @OneToOne(mappedBy = "satellite", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SatelliteState state;
    
    @OneToOne(mappedBy = "satellite", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private EnergySystem energySystem;
    
    @Column(nullable = false)
    private LocalDateTime launchDate;
    
    @Column(nullable = false)
    private boolean operational = true;
    
    public Satellite() {}
    
    public Satellite(String name, SatelliteConstellation constellation, LocalDateTime launchDate) {
        this.name = name;
        this.constellation = constellation;
        this.launchDate = launchDate;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SatelliteConstellation getConstellation() { return constellation; }
    public void setConstellation(SatelliteConstellation constellation) { this.constellation = constellation; }
    public SatelliteState getState() { return state; }
    public void setState(SatelliteState state) { this.state = state; }
    public EnergySystem getEnergySystem() { return energySystem; }
    public void setEnergySystem(EnergySystem energySystem) { this.energySystem = energySystem; }
    public LocalDateTime getLaunchDate() { return launchDate; }
    public void setLaunchDate(LocalDateTime launchDate) { this.launchDate = launchDate; }
    public boolean isOperational() { return operational; }
    public void setOperational(boolean operational) { this.operational = operational; }
}