package com.github.bachelorpraktikum.dbvisualization.view.graph.adapter;

import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import com.github.bachelorpraktikum.dbvisualization.model.Node;

import javax.annotation.Nonnull;

import javafx.geometry.Point2D;

/**
 * A simple implementation of {@link CoordinatesAdapter} which does not respect the real length of
 * edges.
 */
public final class SimpleCoordinatesAdapter implements CoordinatesAdapter {
    @Override
    public double getCalibrationBase() {
        return 1;
    }

    @Nonnull
    @Override
    public Point2D apply(@Nonnull Coordinates coordinates) {
        return new Point2D(coordinates.getX(), coordinates.getY());
    }
}
