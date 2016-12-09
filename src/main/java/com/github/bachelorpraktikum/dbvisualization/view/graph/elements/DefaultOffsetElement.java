package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Point2D;
import javafx.scene.transform.Transform;

class DefaultOffsetElement extends PathElement {
    private final int count;

    DefaultOffsetElement(Element element, ReadOnlyProperty<Transform> parentTransform, CoordinatesAdapter adapter, int count) {
        super(element, parentTransform, adapter);
        this.count = count;
    }

    @Override
    protected Point2D getOffset() {
        Point2D point = super.getOffset();
        point = point.add(point.multiply(count));
        return point;
    }
}
