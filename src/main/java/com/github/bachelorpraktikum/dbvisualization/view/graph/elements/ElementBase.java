package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.view.graph.GraphShapeBase;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Transform;

abstract class ElementBase<T extends Shape> extends GraphShapeBase<Element, T> {

    ElementBase(Element element, ReadOnlyProperty<Transform> parentTransform, CoordinatesAdapter adapter) {
        super(element, parentTransform, adapter);
    }

    public final Element getElement() {
        return getRepresented();
    }

    @Override
    protected final Observable[] getDependencies() {
        return new Observable[]{getElement().stateProperty()};
    }

    final Point2D getNodePosition() {
        return getCoordinatesAdapter().apply(getElement().getNode().getCoordinates());
    }

    @Override
    protected final void displayState(T shape) {
        shape.setFill(getElement().getState().getColor());
    }
}
