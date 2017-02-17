package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;
import javafx.scene.shape.Shape;

class RotatedDefaultOffsetElement extends DefaultOffsetElement {

    RotatedDefaultOffsetElement(Element element, Node node, CoordinatesAdapter adapter, int count) {
        super(element, node, adapter, count);
    }

    @Override
    protected void relocate(Shape shape) {
        super.relocate(shape);
        rotateAccordingToOffset(shape);
    }
}
