package com.github.bachelorpraktikum.dbvisualization.view.graph;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import javafx.geometry.Point2D;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

@ParametersAreNonnullByDefault
class Rail implements Shapeable {
    private static final double CALIBRATION_COEFFICIENT = 0.01;

    private final double calibrationBase;
    @Nonnull
    private final Point2D start;
    @Nonnull
    private final Point2D end;

    Rail(double calibrationBase, Point2D start, Point2D end) {
        this.calibrationBase = calibrationBase;
        this.start = start;
        this.end = end;
    }

    private Shape createBaseShape() {
        Line line = new Line(start.getX(), start.getY(), end.getX(), end.getY());
        line.setStrokeWidth(calibrationBase * CALIBRATION_COEFFICIENT);
        return line;
    }

    @Nonnull
    @Override
    public Shape createShape() {
        Shape shape = createBaseShape();
        // TODO add name / length
        return shape;
    }
}
