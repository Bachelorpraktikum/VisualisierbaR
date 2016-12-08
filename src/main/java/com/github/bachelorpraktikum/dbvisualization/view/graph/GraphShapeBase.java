package com.github.bachelorpraktikum.dbvisualization.view.graph;

import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Transform;

public abstract class GraphShapeBase<T, S extends Shape> extends SimpleObjectProperty<Shape> implements GraphShape<T>, Callable<Shape> {
    private final T represented;
    private final ReadOnlyProperty<Transform> parentTransform;
    private final CoordinatesAdapter adapter;
    private ChangeListener<Transform> listener;

    @Nullable
    private S shape;

    protected GraphShapeBase(T represented, ReadOnlyProperty<Transform> parentTransform, CoordinatesAdapter adapter) {
        this.represented = represented;
        this.parentTransform = parentTransform;
        this.adapter = adapter;
        bind(getBinding());
    }

    private Binding<Shape> getBinding() {
        return Bindings.createObjectBinding(this, getDependencies());
    }

    @Override
    public final T getRepresented() {
        return represented;
    }

    protected final CoordinatesAdapter getCoordinatesAdapter() {
        return adapter;
    }

    protected final double getCalibrationBase() {
        return getCoordinatesAdapter().getCalibrationBase();
    }

    protected Point2D getOffset() {
        return new Point2D(0.4, 0.4);
    }

    protected final ReadOnlyProperty<Transform> parentTransformProperty() {
        return parentTransform;
    }

    @Override
    public final Shape call() throws Exception {
        if (shape == null) {
            shape = createShape();
            resize(shape);
            relocate(shape);
            shape.getTransforms().add(parentTransform.getValue());
            listener = (observable, oldValue, newValue) -> {
                if (oldValue != null) {
                    shape.getTransforms().remove(oldValue);
                }
                shape.getTransforms().add(newValue);
                relocate(shape);
            };
            parentTransform.addListener(new WeakChangeListener<>(listener));
            shape.setOnMouseClicked(event -> System.out.println("CLICK: " + getRepresented()));
        }

        displayState(shape);
        return shape;
    }

    protected abstract void relocate(S shape);

    protected abstract void resize(S shape);

    protected abstract void displayState(S shape);

    @Nonnull
    protected abstract S createShape();

    protected abstract Observable[] getDependencies();
}
