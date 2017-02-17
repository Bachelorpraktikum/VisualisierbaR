package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;
import javafx.geometry.Point2D;
import javafx.scene.shape.Shape;

final class MagnetElement extends PathElement {

    MagnetElement(Element element, Node node, CoordinatesAdapter adapter) {
        super(element, node, adapter);
    }

    @Override
    protected Point2D getOffset() {
        return Point2D.ZERO;
    }

    @Override
    protected void relocate(Shape shape) {
        super.relocate(shape);
        Point2D offset = super.getOffset();
        Point2D realOffset = offset.normalize().multiply(shape.getBoundsInParent().getHeight() / 2);
        shape.setTranslateX(realOffset.getX());
        shape.setTranslateY(realOffset.getY());
        rotateAccordingToOffset(shape, offset);
    }
}
