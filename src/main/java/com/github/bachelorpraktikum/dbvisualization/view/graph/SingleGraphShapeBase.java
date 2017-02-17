package com.github.bachelorpraktikum.dbvisualization.view.graph;

import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;
import java.util.Collections;
import java.util.List;
import javafx.scene.shape.Shape;

public abstract class SingleGraphShapeBase<R, S extends Shape> extends GraphShapeBase<R, S> {

    private final R represented;

    protected SingleGraphShapeBase(R represented, CoordinatesAdapter adapter) {
        super(adapter);
        this.represented = represented;
    }

    protected final R getRepresented() {
        return represented;
    }

    @Override
    public List<R> getRepresentedObjects() {
        return Collections.singletonList(represented);
    }

    @Override
    public final Shape getShape(R represented) {
        return getShape();
    }
}
