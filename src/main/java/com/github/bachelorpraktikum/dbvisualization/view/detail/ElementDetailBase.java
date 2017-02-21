package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.GraphObject;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Shape;
import javax.annotation.Nullable;

public abstract class ElementDetailBase<E extends GraphObject<?>> {

    private final E element;
    private final IntegerProperty time;

    ElementDetailBase(E element, IntegerProperty time) {
        this.element = element;
        this.time = time;
    }

    E getElement() {
        return element;
    }

    abstract String getName();

    @Nullable
    abstract Point2D getCoordinates();

    String getCoordinatesString(Point2D coord) {
        if (coord == null) {
            return ResourceBundle.getBundle("bundles.localization").getString("unavailable");
        } else {
            return String.format("x: %f | y: %f", coord.getX(), coord.getY());
        }
    }

    abstract boolean isTrain();

    ReadOnlyIntegerProperty timeProperty() {
        return time;
    }

    protected Shape getShape() {
        return getElement().getShapeable().createIconShape();
    }

    public static ElementDetailBase create(GraphObject object, IntegerProperty timeProperty) {
        if (object instanceof Train) {
            Train train = (Train) object;
            return new TrainDetail(train, timeProperty);
        } else if (object instanceof Element) {
            Element element = (Element) object;
            return new ElementDetail(element, timeProperty);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
