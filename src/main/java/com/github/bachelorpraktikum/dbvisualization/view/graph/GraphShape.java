package com.github.bachelorpraktikum.dbvisualization.view.graph;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.shape.Shape;

public interface GraphShape<T> extends ReadOnlyProperty<Shape> {
    T getRepresented();
}
