package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.view.TooltipUtil;
import com.github.bachelorpraktikum.dbvisualization.view.graph.GraphShapeBase;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Shape;

abstract class ElementBase<T extends Node> extends GraphShapeBase<Element, T> {

    protected static final double MAX_ELEMENT_WIDTH = 0.3;

    private final List<Element> elements;
    private final com.github.bachelorpraktikum.dbvisualization.model.Node node;
    private final List<ChangeListener<Element.State>> listeners;

    ElementBase(List<Element> elements,
        com.github.bachelorpraktikum.dbvisualization.model.Node node, CoordinatesAdapter adapter) {
        super(adapter);
        this.elements = new ArrayList<>(elements);
        this.node = node;
        listeners = new ArrayList<>(elements.size());
        for (Element element : elements) {
            ChangeListener<Element.State> listener = (observable, oldValue, newValue) -> {
                displayState(element);
            };
            listeners.add(listener);
            element.stateProperty().addListener(new WeakChangeListener<>(listener));
        }
    }

    @Override
    public List<Element> getRepresentedObjects() {
        return elements;
    }

    private com.github.bachelorpraktikum.dbvisualization.model.Node getNode() {
        return node;
    }

    final Point2D getNodePosition() {
        return getCoordinatesAdapter().apply(getNode());
    }

    @Override
    protected Point2D getOffset() {
        com.github.bachelorpraktikum.dbvisualization.model.Node node = getNode();

        List<Point2D> otherVecs = node.getEdges().stream()
            .map(edge -> edge.getNode1().equals(node) ? edge.getNode2() : edge.getNode1())
            .map(getCoordinatesAdapter())
            .map(point -> point.subtract(getNodePosition()))
            .map(Point2D::normalize)
            .collect(Collectors.toList());

        Point2D nearVec = otherVecs.stream()
            .reduce(Point2D::add)
            .orElse(Point2D.ZERO)
            .normalize();

        if (nearVec.equals(Point2D.ZERO)) {
            Point2D other = otherVecs.get(0);
            Point2D result = new Point2D(other.getY(), -other.getX()).normalize()
                .multiply(super.getOffset().magnitude());
            if (result.getX() < 0) {
                result = result.multiply(-1);
            } else if (result.getY() < 0) {
                result = result.multiply(-1);
            }

            return result;
        }

        return nearVec.multiply(-1.0).multiply(super.getOffset().magnitude());
    }

    @Override
    protected void initializedShape(T node) {
        for (Element element : elements) {
            Shape elementShape = getShape(element);
            TooltipUtil.install(elementShape, new Tooltip(element.getName()));
            displayState(element);
        }
    }

    protected final void displayState(Element element) {
        getShape(element).setFill(element.getState().getColor());
    }

    protected final void rotateAccordingToOffset(T node) {
        rotateAccordingToOffset(node, getOffset());
    }

    protected final void rotateAccordingToOffset(T node, Point2D offset) {
        double angle = new Point2D(0, 1).angle(offset);
        if (offset.getX() > 0) {
            angle = -angle;
        }
        angle += 180;
        node.setRotate(angle);
    }
}
