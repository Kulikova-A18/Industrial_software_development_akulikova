// mission-scheduler/src/main/java/com/example/client/TelemetryClient.java
package com.example.client;

import com.example.entity.Satellite;
import com.example.entity.SatelliteState;
import com.example.grpc.TelemetryProto;
import com.example.grpc.TelemetryServiceGrpc;
import com.example.repository.SatelliteRepository;
import com.example.repository.SatelliteStateRepository;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class TelemetryClient {
    private static final Logger logger = LoggerFactory.getLogger(TelemetryClient.class);
    
    private final SatelliteRepository satelliteRepository;
    private final SatelliteStateRepository stateRepository;
    private ManagedChannel channel;
    private TelemetryServiceGrpc.TelemetryServiceStub asyncStub;
    
    @Value("${grpc.telemetry.host:localhost}")
    private String telemetryHost;
    
    @Value("${grpc.telemetry.port:9091}")
    private int telemetryPort;
    
    public TelemetryClient(SatelliteRepository satelliteRepository, SatelliteStateRepository stateRepository) {
        this.satelliteRepository = satelliteRepository;
        this.stateRepository = stateRepository;
    }
    
    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(telemetryHost, telemetryPort)
                .usePlaintext()
                .build();
        asyncStub = TelemetryServiceGrpc.newStub(channel);
        startTelemetryStream();
    }
    
    public void startTelemetryStream() {
        List<Satellite> satellites = satelliteRepository.findAll();
        if (satellites.isEmpty()) {
            logger.warn("No satellites found in database, skipping telemetry stream");
            return;
        }
        
        TelemetryProto.TelemetryRequest request = TelemetryProto.TelemetryRequest.newBuilder()
                .setConstellationName("all")
                .addAllSatelliteNames(satellites.stream().map(Satellite::getName).toList())
                .build();
        
        asyncStub.streamTelemetry(request, new io.grpc.stub.StreamObserver<TelemetryProto.TelemetryUpdate>() {
            @Override
            public void onNext(TelemetryProto.TelemetryUpdate update) {
                logger.debug("Received telemetry: {} - internal={}C, external={}C",
                        update.getSatelliteName(), update.getInternalTemperature(), update.getExternalTemperature());
                
                satelliteRepository.findByName(update.getSatelliteName()).ifPresent(satellite -> {
                    SatelliteState state = stateRepository.findBySatelliteId(satellite.getId()).orElse(null);
                    if (state == null) {
                        state = new SatelliteState();
                        state.setSatellite(satellite);
                        state.setBatteryLevel(update.getBatteryVoltage() * 100 / 28);
                        state.setTemperature(update.getInternalTemperature());
                        state.setIsActive(true);
                        state.setLastUpdateTime(LocalDateTime.now());
                    } else {
                        state.setBatteryLevel(update.getBatteryVoltage() * 100 / 28);
                        state.setTemperature(update.getInternalTemperature());
                        state.setLastUpdateTime(LocalDateTime.now());
                    }
                    stateRepository.save(state);
                });
            }
            
            @Override
            public void onError(Throwable t) {
                logger.error("Telemetry stream error: {}", t.getMessage());
                scheduleReconnect();
            }
            
            @Override
            public void onCompleted() {
                logger.info("Telemetry stream completed");
                scheduleReconnect();
            }
        });
    }
    
    private void scheduleReconnect() {
        try {
            Thread.sleep(5000);
            startTelemetryStream();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            try {
                channel.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}