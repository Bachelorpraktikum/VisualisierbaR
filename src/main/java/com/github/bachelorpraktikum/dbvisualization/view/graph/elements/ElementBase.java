package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.GraphShapeBase;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Transform;

abstract class ElementBase<T extends Shape> extends GraphShapeBase<Element, T> {

    ElementBase(Element element, ReadOnlyProperty<Transform> parentTransform, CoordinatesAdapter adapter) {
        super(element, parentTransform, adapter);
    }

    @Override
    protected final Observable[] getDependencies() {
        return new Observable[]{getRepresented().stateProperty()};
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
            return new Point2D(other.getY(), -other.getX()).normalize().multiply(super.getOffset().magnitude());
        }

        return nearVec.multiply(-1.0).multiply(super.getOffset().magnitude());
    }


    @Override
    protected final void displayState(T shape) {
        shape.setFill(getRepresented().getState().getColor());
    }
}
