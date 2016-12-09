package com.github.bachelorpraktikum.dbvisualization.view.graph;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.shape.Shape;

public interface GraphShape<T> {
    T getRepresented();
    Shape getShape();
}
