package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.model.Switch;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
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
        int index = 0;
        for (Element element : aSwitch.getElements()) {
            if (!element.equals(getMainElement())) {
                others[index++] = adapter.apply(element.getNode());
            }
        }

        Point2D nodePos = getNodePosition();
        Point2D vec1 = others[0].subtract(nodePos);
        Point2D vec2 = others[1].subtract(nodePos);

        Point2D start = nodePos
            .add(vec1.add(vec2).normalize().multiply(0.1 * getCalibrationBase()));
        Point2D fin1 = start.add(vec1.normalize().multiply(0.3 * getCalibrationBase()));
        Point2D fin2 = start.add(vec2.normalize().multiply(0.3 * getCalibrationBase()));

        ObservableList<Double> points = shape.getPoints();
        points.addAll(start.getX(), start.getY(),
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

    @Override
    protected javafx.scene.Node createHighlight(Polygon polygon) {
        Group highlight = new Group();
        for (Element element : getRepresentedObjects()) {
            Node node = element.getNode();
            Point2D nodePos = getCoordinatesAdapter().apply(node);
            Circle nodeCircle = new Circle(0.1 * getCalibrationBase());
            nodeCircle.setCenterX(nodePos.getX());
            nodeCircle.setCenterY(nodePos.getY());

            highlight.getChildren().add(createCircleHighlight(nodeCircle));
        }
        return highlight;
    }

    @Nonnull
    @Override
    protected Polygon createShape() {
        return new Polygon();
    }
}
