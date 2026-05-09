// telemetry-service/src/main/java/com/example/grpc/TelemetryServiceImpl.java
package com.example.grpc;

import com.example.grpc.TelemetryProto.TelemetryRequest;
import com.example.grpc.TelemetryProto.TelemetryUpdate;
import com.example.grpc.TelemetryServiceGrpc.TelemetryServiceImplBase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@GrpcService
public class TelemetryServiceImpl extends TelemetryServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(TelemetryServiceImpl.class);
    private final Random random = new Random();

    private static class SatelliteEmulator {
        final String name;
        final String constellation;
        double internalTemp;
        double externalTemp;
        double batteryVoltage;

        SatelliteEmulator(String name, String constellation) {
            this.name = name;
            this.constellation = constellation;
            this.internalTemp = 20.0 + Math.random() * 10;
            this.externalTemp = -50.0 + Math.random() * 100;
            this.batteryVoltage = 28.0 + Math.random() * 4;
        }

        TelemetryUpdate generateUpdate() {
            internalTemp += (random.nextDouble() - 0.5) * 0.5;
            externalTemp += (random.nextDouble() - 0.5) * 2.0;
            batteryVoltage += (random.nextDouble() - 0.5) * 0.1;
            
            internalTemp = Math.max(-30, Math.min(50, internalTemp));
            externalTemp = Math.max(-120, Math.min(80, externalTemp));
            batteryVoltage = Math.max(22, Math.min(34, batteryVoltage));
            
            return TelemetryUpdate.newBuilder()
                    .setSatelliteName(name)
                    .setConstellationName(constellation)
                    .setInternalTemperature(Math.round(internalTemp * 10) / 10.0)
                    .setExternalTemperature(Math.round(externalTemp * 10) / 10.0)
                    .setBatteryVoltage(Math.round(batteryVoltage * 100) / 100.0)
                    .setTimestamp(System.currentTimeMillis())
                    .build();
        }
    }

    @Override
    public void streamTelemetry(TelemetryRequest request, StreamObserver<TelemetryUpdate> responseObserver) {
        logger.info("Stream telemetry request received: constellation={}, satellites={}",
                request.getConstellationName(), request.getSatelliteNamesList());

        List<String> satelliteNames = request.getSatelliteNamesList();
        if (satelliteNames.isEmpty()) {
            satelliteNames = List.of("Satellite-1", "Satellite-2", "Satellite-3");
        }

        List<SatelliteEmulator> satellites = satelliteNames.stream()
                .map(name -> new SatelliteEmulator(name, request.getConstellationName()))
                .toList();

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        AtomicBoolean cancelled = new AtomicBoolean(false);

        executor.scheduleAtFixedRate(() -> {
            if (cancelled.get()) {
                return;
            }
            try {
                for (SatelliteEmulator sat : satellites) {
                    TelemetryUpdate update = sat.generateUpdate();
                    responseObserver.onNext(update);
                    logger.debug("Sent telemetry for {}: internal={}C, external={}C, battery={}V",
                            sat.name, update.getInternalTemperature(),
                            update.getExternalTemperature(), update.getBatteryVoltage());
                }
            } catch (Exception e) {
                logger.error("Error sending telemetry update", e);
                if (!cancelled.get()) {
                    responseObserver.onError(e);
                    cancelled.set(true);
                    executor.shutdown();
                }
            }
        }, 0, 2, TimeUnit.SECONDS);

        responseObserver.setOnCancelHandler(() -> {
            logger.info("Client cancelled telemetry stream");
            cancelled.set(true);
            executor.shutdown();
        });
    }
}