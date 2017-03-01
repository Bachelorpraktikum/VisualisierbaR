package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import com.github.bachelorpraktikum.dbvisualization.model.Element;
import javafx.beans.property.IntegerProperty;
import javafx.geometry.Point2D;

public class ElementDetail extends ElementDetailBase<Element> {

    public ElementDetail(Element element, IntegerProperty time) {
        super(element, time);
    }

    @Override
    String getName() {
        try {
            String[] names = getElement().getName().split("_");
            return names[names.length - 1];
        } catch (IndexOutOfBoundsException ignored) {
            return getElement().getName();
        }
    }

    @Override
    Point2D getCoordinates() {
        Coordinates coordinates = getElement().getNode().getCoordinates();
        return new Point2D(coordinates.getX(), coordinates.getY());
    }

    @Override
    boolean isTrain() {
        return false;
    }

    Element.State getState() {
        return getElement().getState();
    }
}
