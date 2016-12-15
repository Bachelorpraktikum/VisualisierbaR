package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.model.Switch;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import javax.annotation.Nonnull;

import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Transform;

final class WeichenpunktElement extends SingleElementBase<Polygon> {
    WeichenpunktElement(Element element, Node node, CoordinatesAdapter adapter) {
        super(element, node, adapter);
    }

    @Override
    protected void relocate(Polygon shape) {
        Switch swit = getElement().getSwitch().get();

        CoordinatesAdapter adapter = getCoordinatesAdapter();
        Point2D[] others = new Point2D[2];
        int i = 0;
        for (Element element : swit.getElements()) {
            if (!element.equals(getElement())) {
                others[i++] = adapter.apply(element.getNode().getCoordinates());
            }
        }

        Point2D nodePos = getNodePosition();
        Point2D vec1 = others[0].subtract(nodePos);
        Point2D vec2 = others[1].subtract(nodePos);

        Point2D start = nodePos.add(vec1.add(vec2).normalize().multiply(0.1 * getCalibrationBase()));
        Point2D fin1 = start.add(vec1.normalize().multiply(0.3 * getCalibrationBase()));
        Point2D fin2 = start.add(vec2.normalize().multiply(0.3 * getCalibrationBase()));

        ObservableList<Double> p = shape.getPoints();
        p.addAll(start.getX(), start.getY(),
                fin1.getX(), fin1.getY(),
                fin2.getX(), fin2.getY());
    }

    @Override
    protected Point2D getOffset() {
        return Point2D.ZERO;
    }

    @Override
    protected void resize(Polygon shape) {
    }

    @Nonnull
    @Override
    protected Polygon createShape() {
        return new Polygon();
    }
}
