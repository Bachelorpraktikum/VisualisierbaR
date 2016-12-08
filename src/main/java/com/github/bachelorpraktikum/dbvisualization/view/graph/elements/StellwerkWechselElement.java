package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Transform;

class StellwerkWechselElement extends PathElement {
    StellwerkWechselElement(Element element, ReadOnlyProperty<Transform> parentTransform, CoordinatesAdapter adapter) {
        super(element, parentTransform, adapter);
    }

    @Override
    protected void resize(Shape shape) {
        super.resize(shape);
        shape.setScaleY(shape.getScaleY() * 1.5);
    }

    @Override
    protected Point2D getOffset() {
        return Point2D.ZERO;
    }
}
