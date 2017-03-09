package com.github.bachelorpraktikum.dbvisualization.datasource;

public class LiveTrain {

    private String name;
    private int emergCount;
    private int fahrCounter;
    private int v;
    private String accelState;

    public String getReadableName() {
        return name;
    }

    public int getEmergCount() {
        return emergCount;
    }

    public int getFahrCounter() {
        return fahrCounter;
    }

    public int getV() {
        return v;
    }

    public String getAccelState() {
        return accelState;
    }
}
