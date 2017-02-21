package com.github.bachelorpraktikum.dbvisualization.model;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import javafx.beans.property.Property;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.shape.Shape;

public interface Shapeable<S extends Shape> {

    public enum State {
        ENABLED, DISABLED, AUTO
    }

    String getName();

    S createShape();

    default Shape createIconShape() {
        return createShape();
    }

    Property<State> stateProperty();

    default double minSize() {
        return 20;
    }

    default boolean isVisible(Bounds bounds) {
        if (bounds == null) {
            return true;
        }
        State state = stateProperty().getValue();
        switch (state) {
            case ENABLED:
                return true;
            case DISABLED:
                return false;
            case AUTO:
            default:
                return true; // TODO calculate
        }
    }

    static Shape createShape(URL... urls) {
        return createShape(Arrays.asList(urls));
    }

    static Shape createShape(Collection<URL> urls) {
        try {
            Shape shape = null;

            for (URL url : urls) {
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
