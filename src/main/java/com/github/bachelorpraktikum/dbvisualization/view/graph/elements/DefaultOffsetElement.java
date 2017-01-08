package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import javafx.geometry.Point2D;

class DefaultOffsetElement extends PathElement {
    private final int count;

    DefaultOffsetElement(Element element, Node node, CoordinatesAdapter adapter, int count) {
        super(element, node, adapter);
        this.count = count;
    }

    @Override
    protected Point2D getOffset() {
        Point2D point = super.getOffset();
        point = point.add(point.multiply(count));
        return point;
    }
}
