package com.satellite.service;

import com.satellite.dto.SatellitesOrderResponse;
import com.satellite.model.Satellite;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SatelliteService {

    private final Map<Long, Satellite> satelliteStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public SatelliteService() {
        initTestData();
    }

    private void initTestData() {
        List<Satellite> testSatellites = Arrays.asList(
            new Satellite(idGenerator.getAndIncrement(), "Starlink-1001", 550.0, 53.0, 7.8, 
                LocalDateTime.now().minusDays(30), "ACTIVE"),
            new Satellite(idGenerator.getAndIncrement(), "Starlink-1002", 540.0, 53.2, 7.9,
                LocalDateTime.now().minusDays(25), "ACTIVE"),
            new Satellite(idGenerator.getAndIncrement(), "GPS-III-01", 20200.0, 55.0, 3.9,
                LocalDateTime.now().minusDays(100), "ACTIVE"),
            new Satellite(idGenerator.getAndIncrement(), "OneWeb-001", 1200.0, 87.4, 7.3,
                LocalDateTime.now().minusDays(50), "ACTIVE"),
            new Satellite(idGenerator.getAndIncrement(), "ISS", 408.0, 51.6, 7.66,
                LocalDateTime.now().minusDays(1000), "ACTIVE")
        );

        testSatellites.forEach(sat -> satelliteStore.put(sat.getId(), sat));
        log.info("Initialized {} test satellites", testSatellites.size());
    }

    public List<Satellite> getAllSatellites() {
        log.debug("Fetching all satellites. Total count: {}", satelliteStore.size());
        return new ArrayList<>(satelliteStore.values());
    }

    public Optional<Satellite> getSatelliteById(Long id) {
        log.debug("Fetching satellite by id: {}", id);
        return Optional.ofNullable(satelliteStore.get(id));
    }

    public Satellite createSatellite(Satellite satellite) {
        satellite.setId(idGenerator.getAndIncrement());
        if (satellite.getLaunchDate() == null) {
            satellite.setLaunchDate(LocalDateTime.now());
        }
        if (satellite.getStatus() == null) {
            satellite.setStatus("ACTIVE");
        }
        satelliteStore.put(satellite.getId(), satellite);
        log.info("Created new satellite: {} (ID: {})", satellite.getName(), satellite.getId());
        return satellite;
    }

    public Satellite updateSatellite(Long id, Satellite satellite) {
        if (!satelliteStore.containsKey(id)) {
            log.warn("Satellite with id {} not found for update", id);
            throw new NoSuchElementException("Satellite not found with id: " + id);
        }
        satellite.setId(id);
        satelliteStore.put(id, satellite);
        log.info("Updated satellite ID: {}", id);
        return satellite;
    }

    public boolean deleteSatellite(Long id) {
        Satellite removed = satelliteStore.remove(id);
        if (removed != null) {
            log.info("Deleted satellite: {} (ID: {})", removed.getName(), id);
            return true;
        }
        log.warn("Satellite with id {} not found for deletion", id);
        return false;
    }

    public SatellitesOrderResponse checkSatellitesOrder(String sortBy) {
        log.info("Checking satellite order by parameter: {}", sortBy);
        
        List<Satellite> satellites = getAllSatellites();
        
        if (satellites.isEmpty()) {
            return createResponse(false, "NOT_ORDERED", sortBy, 
                Collections.emptyList(), "No satellites available");
        }

        List<Satellite> sorted = new ArrayList<>(satellites);

        final String effectiveSortBy;
        
        switch (sortBy.toLowerCase()) {
            case "speed":
                sorted.sort(Comparator.comparingDouble(Satellite::getSpeed));
                effectiveSortBy = "speed";
                break;
            case "inclination":
                sorted.sort(Comparator.comparingDouble(Satellite::getInclination));
                effectiveSortBy = "inclination";
                break;
            case "name":
                sorted.sort(Comparator.comparing(Satellite::getName));
                effectiveSortBy = "name";
                break;
            case "altitude":
            default:
                sorted.sort(Comparator.comparingDouble(Satellite::getAltitude));
                effectiveSortBy = "altitude";
                break;
        }

        boolean isOrdered = isListOrdered(satellites, sorted);
        
        String orderType;
        if (isOrdered) {
            double firstVal = getSortValue(satellites.get(0), effectiveSortBy);
            double lastVal = getSortValue(satellites.get(satellites.size()-1), effectiveSortBy);
            orderType = firstVal <= lastVal ? "ASCENDING" : "DESCENDING";
        } else {
            orderType = "NOT_ORDERED";
        }

        List<SatellitesOrderResponse.SatelliteInfo> satelliteInfos = satellites.stream()
            .map(sat -> {
                SatellitesOrderResponse.SatelliteInfo info = new SatellitesOrderResponse.SatelliteInfo();
                info.setId(sat.getId());
                info.setName(sat.getName());
                info.setValue(getSortValue(sat, effectiveSortBy));
                return info;
            })
            .collect(Collectors.toList());

        return createResponse(isOrdered, orderType, effectiveSortBy, satelliteInfos, 
            isOrdered ? "Satellites are in " + orderType.toLowerCase() + " order" : 
            "Satellites are not in order");
    }

    private double getSortValue(Satellite sat, String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "altitude" -> sat.getAltitude();
            case "speed" -> sat.getSpeed();
            case "inclination" -> sat.getInclination();
            default -> sat.getAltitude();
        };
    }

    private boolean isListOrdered(List<Satellite> original, List<Satellite> sorted) {
        for (int i = 0; i < original.size(); i++) {
            if (!original.get(i).getId().equals(sorted.get(i).getId())) {
                return false;
            }
        }
        return true;
    }

    private SatellitesOrderResponse createResponse(boolean ordered, String orderType, 
                                                   String sortParameter,
                                                   List<SatellitesOrderResponse.SatelliteInfo> satellites,
                                                   String message) {
        SatellitesOrderResponse response = new SatellitesOrderResponse();
        response.setOrdered(ordered);
        response.setOrderType(orderType);
        response.setSortParameter(sortParameter);
        response.setSatellites(satellites);
        response.setMessage(message);
        return response;
    }
}