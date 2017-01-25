package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public abstract class ElementDetailBase {
    private int time;

    abstract String getName();

    abstract List<URL> getImageUrls();

    abstract Point2D getCoordinates();

    String getCoordinatesString() {
        Point2D coord = getCoordinates();

        return String.format("x: %f | y: %f", coord.getX(), coord.getY());
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
        } catch (IOException | IllegalStateException e) {
            return new Rectangle(20, 20);
            // e.printStackTrace();
            // throw new IllegalStateException(e);
        }
    }
}
