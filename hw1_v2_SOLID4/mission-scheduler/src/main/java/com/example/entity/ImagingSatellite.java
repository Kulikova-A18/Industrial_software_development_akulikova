// com/example/entity/ImagingSatellite.java
package com.example.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("IMAGING")
public class ImagingSatellite extends Satellite {
    
    private Double resolution;
    private String sensorType;
    
    public ImagingSatellite() {}
    
    public Double getResolution() { return resolution; }
    public void setResolution(Double resolution) { this.resolution = resolution; }
    public String getSensorType() { return sensorType; }
    public void setSensorType(String sensorType) { this.sensorType = sensorType; }
}