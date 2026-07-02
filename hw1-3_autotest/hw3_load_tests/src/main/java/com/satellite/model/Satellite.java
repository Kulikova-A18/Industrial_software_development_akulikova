package com.satellite.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Satellite {
    private Long id;
    private String name;
    private double altitude;
    private double inclination;
    private double speed;
    private LocalDateTime launchDate;
    private String status; // ACTIVE, INACTIVE, DECAYED
}