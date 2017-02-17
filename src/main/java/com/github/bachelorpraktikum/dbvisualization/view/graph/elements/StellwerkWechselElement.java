package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;
import javafx.geometry.Point2D;
import javafx.scene.shape.Shape;

class StellwerkWechselElement extends PathElement {

    StellwerkWechselElement(Element element, Node node, CoordinatesAdapter adapter) {
        super(element, node, adapter);
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
