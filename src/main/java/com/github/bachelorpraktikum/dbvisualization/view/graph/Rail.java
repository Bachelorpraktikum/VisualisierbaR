package com.github.bachelorpraktikum.dbvisualization.view.graph;

import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import javax.annotation.Nonnull;

import javafx.geometry.Point2D;
import javafx.scene.shape.Line;

final class Rail extends SingleGraphShapeBase<Edge, Line> {
    private static final double CALIBRATION_COEFFICIENT = 0.05;

    protected Rail(Edge edge, CoordinatesAdapter adapter) {
        super(edge, adapter);
    }

    @Override
    protected void relocate(Line shape) {
        CoordinatesAdapter adapter = getCoordinatesAdapter();
        Point2D start = adapter.apply(getRepresented().getNode1().getCoordinates());
        Point2D end = adapter.apply(getRepresented().getNode2().getCoordinates());
        shape.setStartX(start.getX());
        shape.setStartY(start.getY());
        shape.setEndX(end.getX());
        shape.setEndY(end.getY());
    }

    @Override
    protected void resize(Line line) {
        line.setStrokeWidth(getCalibrationBase() * CALIBRATION_COEFFICIENT);
    }

    @Nonnull
    @Override
    public Line createShape() {
        return new Line();
    }
}
