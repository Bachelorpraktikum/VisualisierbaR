package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.model.Element;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class GraphObject<T> {
    private static final Object TRAINS = new Object();
    private final T t;
    private final Shape image;
    private final String name;

    private GraphObject(T t, Shape image, String name) {
        this.t = t;
        this.image = image;
        this.name = name;
    }

    public static GraphObject<Element.Type> element(Element.Type elementType) {
        Shape shape = getShape(elementType);
        return new GraphObject<>(elementType, shape, elementType.getName());
    }

    private static Shape getShape(Element.Type type) {
        switch (type) {
            case GeschwindigkeitsAnzeigerImpl:
                return new Polygon(0, 1, 1, 0, 1, 1);
            case WeichenPunktImpl:
                return new Polygon(0, 3, 3, 3, 3, 2);
            case VorSignalImpl:
            case HauptSignalImpl:
                Shape shape = getPathShape(type);
                shape.setRotate(90);
                return shape;
            default:
                return getPathShape(type);
        }
    }

    private static Shape getPathShape(Element.Type type) {
        try {
            Shape shape = null;

            for (URL url : type.getImageUrls()) {
                FXMLLoader loader = new FXMLLoader(url);
                if (shape == null) {
                    shape = loader.load();
                } else {
                    shape = Shape.union(shape, loader.load());
                }
            }

            return shape;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public static GraphObject<?> trains() {
        Shape trainSymbol = new Rectangle(20, 20);
        trainSymbol.setFill(Color.GREEN);
        trainSymbol.setOpacity(0.5);
        return new GraphObject<>(TRAINS, trainSymbol, "Züge");
    }

    public T getWrapped() {
        return t;
    }

    public Shape getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GraphObject<?> that = (GraphObject<?>) o;

        return t.equals(that.t);
    }

    @Override
    public int hashCode() {
        return t.hashCode();
    }
}
