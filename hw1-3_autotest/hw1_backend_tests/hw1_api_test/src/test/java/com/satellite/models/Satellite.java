package com.satellite.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Satellite {
    private Long id;
    private String name;
    private double altitude;
    private double inclination;
    private double speed;
    private LocalDateTime launchDate;
    private String status;
}