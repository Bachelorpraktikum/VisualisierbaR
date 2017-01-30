package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import javax.annotation.Nonnull;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

final class DummyElement extends SingleElementBase<Rectangle> {

    private static final double STROKE_WIDTH = 0.04;

    private final int count;

    DummyElement(Element element, Node node, CoordinatesAdapter adapter, int count) {
        super(element, node, adapter);
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
        Rectangle rectangle = new Rectangle();
        rectangle.setStroke(Color.RED);
        rectangle.setStrokeWidth(getCalibrationBase() * STROKE_WIDTH);
        return rectangle;
    }

    protected double getStrokeFactor() {
        return 0;
    }
}
