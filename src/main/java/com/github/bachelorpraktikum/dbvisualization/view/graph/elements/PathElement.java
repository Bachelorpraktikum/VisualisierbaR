package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javafx.beans.property.ReadOnlyProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Transform;

class PathElement extends ElementBase<Shape> {
    PathElement(Element element, ReadOnlyProperty<Transform> parentTransform, CoordinatesAdapter adapter) {
        super(element, parentTransform, adapter);
    }

    @Override
    protected void relocate(Shape shape) {
        Point2D nodePos = getNodePosition().add(getOffset(), getOffset());
        Point2D parentPos = parentTransformProperty().getValue().transform(nodePos);

        Bounds bounds = shape.getBoundsInLocal();
        double x = parentPos.getX() - (bounds.getWidth()) / 2;
        double y = parentPos.getY() - bounds.getHeight() / 2;

        shape.relocate(x, y);
    }

    @Override
    protected void resize(Shape shape) {
        Bounds bounds = shape.getLayoutBounds();
        double max = Math.max(bounds.getHeight(), bounds.getWidth());
        double desiredMax = 0.5;
        double factor = desiredMax / max;
        double scale = shape.getScaleX() * factor;

        shape.setScaleX(scale);
        shape.setScaleY(scale);
    }

    @Override
    protected Shape createShape() {
        String[] urls = getRepresented().getType().getImageUrls();
        try {
            Shape shape = null;

            for (String url : urls) {
                URI uri = new URI(url);
                FXMLLoader loader = new FXMLLoader(uri.toURL());
                if (shape == null) {
                    shape = loader.load();
                } else {
                    shape = Shape.union(shape, loader.load());
                }
            }

            /*
            System.out.println(shape.getLayoutBounds());
            System.out.println(shape.getBoundsInLocal());
            System.out.println(shape.getBoundsInParent());
*/

            return shape;
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
}
