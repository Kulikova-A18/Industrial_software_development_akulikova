package com.example;

public class ImagingSatellite extends Satellite {
    private int photosTaken;
    private final double resolution;

    public ImagingSatellite(String name, double resolution) {
        super(name, 0.9);
        this.resolution = resolution;
        this.photosTaken = 0;
    }

    @Override
    public String getType() {
        return "Спутник дистанционного зондирования";
    }

    @Override
    public void performSpecificMission() {
        System.out.println(name + ": Съемка территории с разрешением " + resolution + " м/пиксель");
        energy.consume(0.08);
        photosTaken++;
        System.out.println(name + ": Снимок #" + photosTaken + " сделан!");
    }

    public int getPhotosTaken() {
        return photosTaken;
    }

    public double getResolution() {
        return resolution;
    }

    @Override
    public String toString() {
        return super.toString().replace("}", ", photosTaken=" + photosTaken + ", resolution=" + resolution + "}");
    }
}