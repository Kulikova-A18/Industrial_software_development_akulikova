package com.satellite.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SatellitesOrderResponse {
    private boolean ordered;
    private String orderType;
    private String sortParameter;
    private List<SatelliteInfo> satellites;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SatelliteInfo {
        private Long id;
        private String name;
        private double value;
    }
}