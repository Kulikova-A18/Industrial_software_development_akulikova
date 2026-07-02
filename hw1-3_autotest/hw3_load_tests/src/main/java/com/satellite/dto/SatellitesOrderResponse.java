package com.satellite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SatellitesOrderResponse {
    private boolean isOrdered;
    private String orderType; // ASCENDING, DESCENDING, NOT_ORDERED
    private String sortParameter;
    private List<SatelliteInfo> satellites;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SatelliteInfo {
        private Long id;
        private String name;
        private double value;
    }
}