package com.example;

import java.util.ArrayList;
import java.util.List;

public class SatelliteConstellationFactory {

    public static SatelliteConstellation createEmptyConstellation(String name) {
        return new SatelliteConstellation(name);
    }

    public static SatelliteConstellation createCommunicationConstellation(String name, int count,
            double baseBandwidth) {
        SatelliteConstellation constellation = new SatelliteConstellation(name);
        CommunicationSatelliteFactory factory = new CommunicationSatelliteFactory();

        for (int i = 1; i <= count; i++) {
            String satelliteName = name + "-ComSat-" + i;
            double bandwidth = baseBandwidth * (1 + (i * 0.1));
            Satellite satellite = factory.createSatelliteWithParameter(satelliteName, bandwidth);
            constellation.addSatellite(satellite);
        }

        return constellation;
    }

    public static SatelliteConstellation createImagingConstellation(String name, int count, double baseResolution) {
        SatelliteConstellation constellation = new SatelliteConstellation(name);
        ImagingSatelliteFactory factory = new ImagingSatelliteFactory();

        for (int i = 1; i <= count; i++) {
            String satelliteName = name + "-ImgSat-" + i;
            double resolution = baseResolution / i;
            Satellite satellite = factory.createSatelliteWithParameter(satelliteName, resolution);
            constellation.addSatellite(satellite);
        }

        return constellation;
    }

    public static SatelliteConstellation createMixedConstellation(String name,
            int comCount, double bandwidth,
            int imgCount, double resolution) {
        SatelliteConstellation constellation = new SatelliteConstellation(name);

        CommunicationSatelliteFactory comFactory = new CommunicationSatelliteFactory();
        for (int i = 1; i <= comCount; i++) {
            Satellite satellite = comFactory.createSatelliteWithParameter(
                    name + "-Com-" + i, bandwidth * i);
            constellation.addSatellite(satellite);
        }

        ImagingSatelliteFactory imgFactory = new ImagingSatelliteFactory();
        for (int i = 1; i <= imgCount; i++) {
            Satellite satellite = imgFactory.createSatelliteWithParameter(
                    name + "-Img-" + i, resolution / i);
            constellation.addSatellite(satellite);
        }

        return constellation;
    }

    public static class ConstellationBuilder {
        private String name;
        private List<Satellite> satellites = new ArrayList<>();

        public ConstellationBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ConstellationBuilder addSatellite(Satellite satellite) {
            this.satellites.add(satellite);
            return this;
        }

        public ConstellationBuilder addCommunicationSatellite(String name, double bandwidth) {
            CommunicationSatelliteFactory factory = new CommunicationSatelliteFactory();
            this.satellites.add(factory.createSatelliteWithParameter(name, bandwidth));
            return this;
        }

        public ConstellationBuilder addImagingSatellite(String name, double resolution) {
            ImagingSatelliteFactory factory = new ImagingSatelliteFactory();
            this.satellites.add(factory.createSatelliteWithParameter(name, resolution));
            return this;
        }

        public ConstellationBuilder addMultipleSatellites(int count, SatelliteFactory factory, String baseName,
                double baseParam) {
            for (int i = 1; i <= count; i++) {
                String satName = baseName + "-" + i;
                double param = baseParam * i;
                this.satellites.add(factory.createSatelliteWithParameter(satName, param));
            }
            return this;
        }

        public SatelliteConstellation build() {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalStateException("Имя группировки не может быть пустым");
            }

            SatelliteConstellation constellation = new SatelliteConstellation(name);
            for (Satellite satellite : satellites) {
                constellation.addSatellite(satellite);
            }
            return constellation;
        }
    }
}