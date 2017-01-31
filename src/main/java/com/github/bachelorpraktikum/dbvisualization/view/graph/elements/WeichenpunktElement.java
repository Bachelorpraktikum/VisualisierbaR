package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.model.Switch;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;

import javax.annotation.Nonnull;

final class WeichenpunktElement extends ElementBase<Polygon> {
    private final Switch aSwitch;

    WeichenpunktElement(Switch aSwitch, Node node, CoordinatesAdapter adapter) {
        super(aSwitch.getElements(), node, adapter);
        this.aSwitch = aSwitch;
    }

    private Element getMainElement() {
        return aSwitch.getMainElement();
    }

    @Override
    public Shape getShape(Element element) {
        return getShape();
    }

    @Override
    protected void relocate(Polygon shape) {
        CoordinatesAdapter adapter = getCoordinatesAdapter();
        Point2D[] others = new Point2D[2];
        int i = 0;
        for (Element element : aSwitch.getElements()) {
            if (!element.equals(getMainElement())) {
                others[i++] = adapter.apply(element.getNode());
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
    protected double getStrokeFactor() {
        return 0.1 * getCalibrationBase();
    }

    @Override
    protected void resize(Polygon shape) {
        shape.setStrokeWidth(getStrokeFactor());
        shape.setStrokeType(StrokeType.CENTERED);
    }

    @Nonnull
    @Override
    protected Polygon createShape() {
        return new Polygon();
    }
}
