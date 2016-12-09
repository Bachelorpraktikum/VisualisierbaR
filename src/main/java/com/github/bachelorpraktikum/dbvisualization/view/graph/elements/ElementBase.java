package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.GraphShapeBase;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Transform;

abstract class ElementBase<T extends Shape> extends GraphShapeBase<Element, T> {

    private final ChangeListener<Element.State> listener;

    ElementBase(Element element, ReadOnlyProperty<Transform> parentTransform, CoordinatesAdapter adapter) {
        super(element, parentTransform, adapter);
        listener = (observable, oldValue, newValue) -> displayState(getShape());
        getRepresented().stateProperty().addListener(new WeakChangeListener<>(listener));
    }

    final Point2D getNodePosition() {
        return getCoordinatesAdapter().apply(getRepresented().getNode().getCoordinates());
    }

    @Override
    protected Point2D getOffset() {
        Node node = getRepresented().getNode();

        List<Point2D> otherVecs = node.getEdges().stream()
                .map(edge -> edge.getNode1().equals(node) ? edge.getNode2() : edge.getNode1())
                .map(Node::getCoordinates)
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
            Point2D result = new Point2D(other.getY(), -other.getX()).normalize().multiply(super.getOffset().magnitude());
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
    public T initializeShape() {
        T shape = super.initializeShape();
        displayState(shape);
        return shape;
    }

    protected final void displayState(T shape) {
        shape.setFill(getRepresented().getState().getColor());
    }
}
