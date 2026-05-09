// com/example/entity/SatelliteState.java
package com.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "satellite_states")
public class SatelliteState {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "satellite_id", unique = true)
    private Satellite satellite;
    
    @Column(nullable = false)
    private Double batteryLevel;
    
    @Column(nullable = false)
    private Double temperature;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private LocalDateTime lastUpdateTime;
    
    public SatelliteState() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Satellite getSatellite() { return satellite; }
    public void setSatellite(Satellite satellite) { this.satellite = satellite; }
    public Double getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(Double batteryLevel) { this.batteryLevel = batteryLevel; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
}