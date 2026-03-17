package com.example;

public class CommunicationSatellite extends Satellite {
    private final double bandwidth;

    public CommunicationSatellite(String name, double bandwidth) {
        super(name, 0.85);
        this.bandwidth = bandwidth;
    }

    @Override
    public String getType() {
        return "Коммуникационный спутник";
    }

    @Override
    public void performSpecificMission() {
        System.out.println(name + ": Передача данных со скоростью " + bandwidth + " Мбит/с");
        energy.consume(0.05);
        System.out.println(name + ": Отправил " + bandwidth + " Мбит данных!");
    }

    public double getBandwidth() {
        return bandwidth;
    }

    @Override
    public String toString() {
        return super.toString().replace("}", ", bandwidth=" + bandwidth + "}");
    }
}