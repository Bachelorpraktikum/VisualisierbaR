package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Transform;

final class MagnetElement extends PathElement {
    MagnetElement(Element element, ReadOnlyProperty<Transform> parentTransform, CoordinatesAdapter adapter) {
        super(element, parentTransform, adapter);
    }

    @Override
    protected Point2D getOffset() {
        return Point2D.ZERO;
    }

    @Override
    protected void relocate(Shape shape) {
        super.relocate(shape);
        shape.setRotate(180);
        shape.setTranslateY(shape.getBoundsInParent().getHeight() / 2);
    }
}
