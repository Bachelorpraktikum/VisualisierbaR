package com.github.bachelorpraktikum.dbvisualization.view.legend;

import com.github.bachelorpraktikum.dbvisualization.view.GraphObject;

import java.net.URL;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

public class LegendItem {
    private final GraphObject<?> graphObject;
    private final Property<State> stateProperty;

    public LegendItem(GraphObject<?> graphObject) {
        this.graphObject = graphObject;
        this.stateProperty = new SimpleObjectProperty<>(State.AUTO);
    }

    public enum State {
        ENABLED, DISABLED, AUTO
    }

    public URL getImageUrl() {
        return graphObject.getImageUrl();
    }

    public String getName() {
        return graphObject.getName();
    }

    public Property<State> stateProperty() {
        return stateProperty;
    }

    public GraphObject<?> getGraphObject() {
        return graphObject;
    }
}
