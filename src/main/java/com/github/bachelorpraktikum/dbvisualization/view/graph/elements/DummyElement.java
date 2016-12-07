package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;

final class DummyElement extends ElementBase<Rectangle> {
    DummyElement(Element element, ReadOnlyProperty<Transform> parentTransform, CoordinatesAdapter adapter) {
        super(element, parentTransform, adapter);
    }

    @Override
    protected void relocate(Rectangle shape) {
        Point2D pos = getNodePosition().add(getOffset());
        shape.setX(pos.getX());
        shape.setY(pos.getY());
    }

    @Override
    protected void resize(Rectangle shape) {
        double radius = getCalibrationBase() * 0.2;
        shape.setHeight(radius);
        shape.setWidth(radius);
    }

    @Override
    protected Rectangle createShape() {
        return new Rectangle();
    }
}
