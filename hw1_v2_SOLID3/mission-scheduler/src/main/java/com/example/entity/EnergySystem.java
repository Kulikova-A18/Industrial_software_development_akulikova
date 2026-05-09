// com/example/entity/EnergySystem.java
package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "energy_systems")
public class EnergySystem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "satellite_id", unique = true)
    private Satellite satellite;
    
    @Column(nullable = false)
    private Double solarPanelEfficiency;
    
    @Column(nullable = false)
    private Double batteryCapacity;
    
    @Column(nullable = false)
    private Integer numberOfSolarPanels;
    
    public EnergySystem() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Satellite getSatellite() { return satellite; }
    public void setSatellite(Satellite satellite) { this.satellite = satellite; }
    public Double getSolarPanelEfficiency() { return solarPanelEfficiency; }
    public void setSolarPanelEfficiency(Double solarPanelEfficiency) { this.solarPanelEfficiency = solarPanelEfficiency; }
    public Double getBatteryCapacity() { return batteryCapacity; }
    public void setBatteryCapacity(Double batteryCapacity) { this.batteryCapacity = batteryCapacity; }
    public Integer getNumberOfSolarPanels() { return numberOfSolarPanels; }
    public void setNumberOfSolarPanels(Integer numberOfSolarPanels) { this.numberOfSolarPanels = numberOfSolarPanels; }
}