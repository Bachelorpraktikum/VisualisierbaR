package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.shape.Shape;
import javax.annotation.Nonnull;

class PathElement extends SingleElementBase<Shape> {

    PathElement(Element element, Node node, CoordinatesAdapter adapter) {
        super(element, node, adapter);
    }

    @Override
    protected void relocate(Shape shape) {
        Point2D nodePos = getNodePosition().add(getOffset());

        Bounds bounds = shape.getBoundsInLocal();
        double x = nodePos.getX() - (bounds.getWidth()) / 2;
        double y = nodePos.getY() - bounds.getHeight() / 2;

        shape.relocate(x, y);
    }

    protected double getDesiredMax() {
        return 0.5 * getCalibrationBase();
    }

    @Override
    protected void resize(Shape shape) {
        Bounds bounds = shape.getLayoutBounds();
        double max = Math.max(bounds.getHeight(), bounds.getWidth());
        double factor = getDesiredMax() / max;
        double scale = shape.getScaleX() * factor;

        shape.setScaleX(scale);
        shape.setScaleY(scale);
    }

    @Nonnull
    @Override
    protected Shape createShape() {
        return getElement().createShape();
    }
}
