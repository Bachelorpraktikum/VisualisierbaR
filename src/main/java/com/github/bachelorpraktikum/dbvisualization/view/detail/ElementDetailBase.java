package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;

import java.net.URL;

abstract class ElementDetailBase {
    private int time;

    abstract String getName();

    abstract URL getImageURL();

    abstract Coordinates getCoordinates();

    abstract boolean isTrain();

    void setTime(int time) {
        this.time = time;
    }

    int getTime() {
        return time;
    }
}
