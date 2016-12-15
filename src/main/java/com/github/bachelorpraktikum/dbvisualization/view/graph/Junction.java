package com.github.bachelorpraktikum.dbvisualization.view.graph;

import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.TooltipUtil;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import javax.annotation.Nonnull;

import javafx.geometry.Point2D;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;

final class Junction extends SingleGraphShapeBase<Node, Circle> {
    private static final double CALIBRATION_COEFFICIENT = 0.1;

    Junction(Node node, CoordinatesAdapter adapter) {
        super(node, adapter);
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

    @Override
    protected void initializedShape(Circle circle) {
        TooltipUtil.install(circle, new Tooltip(getRepresented().getName()));
    }

    @Nonnull
    @Override
    public Circle createShape() {
        return new Circle();
    }
}
