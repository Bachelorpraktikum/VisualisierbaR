package com.github.bachelorpraktikum.dbvisualization.view.graph;

import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.transform.Transform;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class GraphShapeBase<R, S extends Node> implements GraphShape<R> {

    private final CoordinatesAdapter adapter;
    private ChangeListener<Transform> listener;

    @Nullable
    private S shape;

    protected GraphShapeBase(CoordinatesAdapter adapter) {
        this.adapter = adapter;
    }

    protected S initializeShape() {
        S shape = createShape();
        resize(shape);
        relocate(shape);
        // shape.setOnMouseClicked(event -> System.out.println("CLICK: " + getRepresented()));
        return shape;
    }

    protected void initializedShape(S s) {
    }

    @Override
    public final S getShape() {
        if (shape == null) {
            shape = initializeShape();
            initializedShape(shape);
        }

        return shape;
    }

    protected final CoordinatesAdapter getCoordinatesAdapter() {
        return adapter;
    }

    protected final double getCalibrationBase() {
        return getCoordinatesAdapter().getCalibrationBase();
    }

    protected Point2D getOffset() {
        return new Point2D(0.4, 0.4).multiply(getCalibrationBase());
    }

    protected abstract void relocate(S shape);

    protected abstract void resize(S shape);

    @Nonnull
    protected abstract S createShape();
}
