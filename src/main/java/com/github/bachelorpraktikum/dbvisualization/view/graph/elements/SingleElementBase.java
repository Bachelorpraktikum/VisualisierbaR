package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import java.util.Collections;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Transform;

abstract class SingleElementBase<S extends Shape> extends ElementBase<S> {
    SingleElementBase(Element element, Node node, CoordinatesAdapter adapter) {
        super(Collections.singletonList(element), node, adapter);
    }

    protected Element getElement() {
        return getRepresentedObjects().get(0);
    }

    @Override
    public Shape getShape(Element element) {
        return getShape();
    }
}
