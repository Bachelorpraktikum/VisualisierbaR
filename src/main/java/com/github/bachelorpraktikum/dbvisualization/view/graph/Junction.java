package com.github.bachelorpraktikum.dbvisualization.view.graph;

import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import javax.annotation.Nonnull;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Transform;

final class Junction extends GraphShapeBase<Node, Circle> {
    private static final double CALIBRATION_COEFFICIENT = 0.1;

    Junction(Node node, ReadOnlyProperty<Transform> parentTransform, CoordinatesAdapter adapter) {
        super(node, parentTransform, adapter);
    }

    @Override
    protected void relocate(Circle shape) {
        Node node = getRepresented();
        CoordinatesAdapter adapter = getCoordinatesAdapter();
        Point2D position = adapter.apply(node.getCoordinates());
        shape.setCenterX(position.getX());
        shape.setCenterY(position.getY());
    }

    @Override
    protected void resize(Circle shape) {
        double radius = getCalibrationBase() * CALIBRATION_COEFFICIENT;
        shape.setRadius(radius);
    }

    @Nonnull
    @Override
    public Circle createShape() {
        return new Circle();
    }
}
