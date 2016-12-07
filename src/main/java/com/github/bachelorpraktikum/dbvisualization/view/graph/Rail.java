package com.github.bachelorpraktikum.dbvisualization.view.graph;

import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Line;
import javafx.scene.transform.Transform;

final class Rail extends GraphShapeBase<Edge, Line> {
    protected Rail(Edge edge, ReadOnlyProperty<Transform> parentTransform, CoordinatesAdapter adapter) {
        super(edge, parentTransform, adapter);
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
        line.setStrokeWidth(getCalibrationBase() * 0.05);
    }

    @Override
    protected void displayState(Line shape) {
    }

    @Override
    protected Observable[] getDependencies() {
        return new Observable[0];
    }

    @Override
    public Line createShape() {
        return new Line();
    }
}
