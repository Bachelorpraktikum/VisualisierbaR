package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import javax.annotation.Nonnull;

import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;

final class DummyElement extends ElementBase<Rectangle> {
    private final int count;

    DummyElement(Element element, ReadOnlyProperty<Transform> parentTransform, CoordinatesAdapter adapter, int count) {
        super(element, parentTransform, adapter);
        this.count = count;
    }

    @Override
    protected void relocate(Rectangle shape) {
        Point2D pos = getNodePosition().add(getOffset());
        shape.setX(pos.getX() - shape.getWidth() / 2);
        shape.setY(pos.getY() - shape.getHeight() / 2);
    }

    @Override
    protected Point2D getOffset() {
        Point2D point = super.getOffset();
        point = point.add(point.multiply(count));
        return point;
    }

    @Override
    protected void resize(Rectangle shape) {
        double radius = getCalibrationBase() * 0.2;
        shape.setHeight(radius);
        shape.setWidth(radius);
    }

    @Nonnull
    @Override
    protected Rectangle createShape() {
        return new Rectangle();
    }
}
