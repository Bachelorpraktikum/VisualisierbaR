package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.shape.Shape;

public abstract class ElementDetailBase {
    private int time;

    abstract String getName();

    abstract List<URL> getImageUrls();

    abstract Coordinates getCoordinates();

    String getCoordinatesString() {
        Coordinates coord = getCoordinates();

        return String.format("x: %d | y: %d", coord.getX(), coord.getY());
    }

    abstract boolean isTrain();

    void setTime(int time) {
        this.time = time;
    }

    int getTime() {
        return time;
    }

    protected Shape getShape() {
        try {
            Shape shape = null;

            for (URL url : getImageUrls()) {
                FXMLLoader loader = new FXMLLoader(url);
                if (shape == null) {
                    shape = loader.load();
                } else {
                    shape = Shape.union(shape, loader.load());
                }
            }

            return shape;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
}
