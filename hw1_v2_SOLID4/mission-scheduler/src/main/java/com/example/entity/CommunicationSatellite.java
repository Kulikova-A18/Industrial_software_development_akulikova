// com/example/entity/CommunicationSatellite.java
package com.example.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("COMMUNICATION")
public class CommunicationSatellite extends Satellite {
    
    private Integer transponderCount;
    private Double frequencyBand;
    
    public CommunicationSatellite() {}
    
    public Integer getTransponderCount() { return transponderCount; }
    public void setTransponderCount(Integer transponderCount) { this.transponderCount = transponderCount; }
    public Double getFrequencyBand() { return frequencyBand; }
    public void setFrequencyBand(Double frequencyBand) { this.frequencyBand = frequencyBand; }
}