package com.github.bachelorpraktikum.dbvisualization.view.legend;

import com.github.bachelorpraktikum.dbvisualization.model.Element;

import java.net.URL;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

public class LegendItem {
    private final URL url;
    private final Property<State> stateProperty;
    private final String name;

    public LegendItem(URL url, String name) {
        this.url = url;
        this.stateProperty = new SimpleObjectProperty<>(State.AUTO);
        this.name = name;
    }

    public enum Type {
        TRAIN, ELEMENT
    }

    public enum State {
        ENABLED, DISABLED, AUTO
    }

    public URL getImageUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public Property<State> stateProperty() {
        return stateProperty;
    }
}
