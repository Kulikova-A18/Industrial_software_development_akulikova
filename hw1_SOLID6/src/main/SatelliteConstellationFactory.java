package com.example.factories;


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
            CommunicationSatelliteParam param = new CommunicationSatelliteParam(
                satelliteName, 0.85, bandwidth
            );
            Satellite satellite = factory.createSatelliteWithParameter(param);
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
            ImagingSatelliteParam param = new ImagingSatelliteParam(
                satelliteName, 0.9, resolution
            );
            Satellite satellite = factory.createSatelliteWithParameter(param);
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
            CommunicationSatelliteParam param = new CommunicationSatelliteParam(
                name + "-Com-" + i, 0.85, bandwidth * i
            );
            Satellite satellite = comFactory.createSatelliteWithParameter(param);
            constellation.addSatellite(satellite);
        }

        ImagingSatelliteFactory imgFactory = new ImagingSatelliteFactory();
        for (int i = 1; i <= imgCount; i++) {
            ImagingSatelliteParam param = new ImagingSatelliteParam(
                name + "-Img-" + i, 0.9, resolution / i
            );
            Satellite satellite = imgFactory.createSatelliteWithParameter(param);
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
            CommunicationSatelliteParam param = new CommunicationSatelliteParam(name, 0.85, bandwidth);
            this.satellites.add(factory.createSatelliteWithParameter(param));
            return this;
        }

        public ConstellationBuilder addImagingSatellite(String name, double resolution) {
            ImagingSatelliteFactory factory = new ImagingSatelliteFactory();
            ImagingSatelliteParam param = new ImagingSatelliteParam(name, 0.9, resolution);
            this.satellites.add(factory.createSatelliteWithParameter(param));
            return this;
        }

        public ConstellationBuilder addMultipleSatellites(int count, SatelliteFactory factory, String baseName,
                double baseParam) {
            for (int i = 1; i <= count; i++) {
                String satName = baseName + "-" + i;
                double param = baseParam * i;
                
                if (factory instanceof CommunicationSatelliteFactory) {
                    CommunicationSatelliteParam comParam = new CommunicationSatelliteParam(
                        satName, 0.85, param
                    );
                    this.satellites.add(factory.createSatelliteWithParameter(comParam));
                } else if (factory instanceof ImagingSatelliteFactory) {
                    ImagingSatelliteParam imgParam = new ImagingSatelliteParam(
                        satName, 0.9, param
                    );
                    this.satellites.add(factory.createSatelliteWithParameter(imgParam));
                }
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