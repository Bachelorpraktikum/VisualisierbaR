package com.github.bachelorpraktikum.dbvisualization.view.graph;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

@ParametersAreNonnullByDefault
class NodeShape implements Shapeable {
    private static final double CALIBRATION_COEFFICIENT = 0.1;
    @Nonnull
    private final Node node;
    @Nonnull
    private final Point2D position;
    private final double calibrationBase;

    @Nullable
    private Shape shape;

    NodeShape(CoordinatesAdapter coordinatesAdapter, Node node) {
        this.node = node;
        this.position = coordinatesAdapter.apply(node);
        this.calibrationBase = coordinatesAdapter.getCalibrationBase();

        ChangeListener<Element.State> listener = new WeakChangeListener<>(
                (observable, oldValue, newValue) -> shape = null
        );
        node.getElements()
                .forEach(element -> element.stateProperty().addListener(listener));
    }

    @Nonnull
    Point2D getPosition() {
        return position;
    }

    private double getCalibrationBase() {
        return calibrationBase;
    }

    private Shape createBaseShape() {
        Point2D position = getPosition();
        double radius = getCalibrationBase() * CALIBRATION_COEFFICIENT;
        return new Circle(position.getX(), position.getY(), radius);
    }

    @Nonnull
    @Override
    public Shape createShape() {
        if (shape != null) {
            return shape;
        }

        Shape shape = createBaseShape();
        // TODO append name / elements
        return this.shape = shape;
    }
}
