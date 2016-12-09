package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import java.io.IOException;
import java.net.URL;

import javax.annotation.Nonnull;

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
        Point2D nodePos = getNodePosition().add(getOffset());
        Point2D parentPos = parentTransformProperty().getValue().transform(nodePos);

        Bounds bounds = shape.getBoundsInLocal();
        double x = parentPos.getX() - (bounds.getWidth()) / 2;
        double y = parentPos.getY() - bounds.getHeight() / 2;

        shape.relocate(x, y);
    }

    protected double getDesiredMax() {
        return 0.5 * getCalibrationBase();
    }

    @Override
    protected void resize(Shape shape) {
        Bounds bounds = shape.getLayoutBounds();
        double max = Math.max(bounds.getHeight(), bounds.getWidth());
        double factor = getDesiredMax() / max;
        double scale = shape.getScaleX() * factor;

        shape.setScaleX(scale);
        shape.setScaleY(scale);
    }

    protected URL[] getImageUrls() {
        return getRepresented().getType().getImageUrls();
    }

    @Nonnull
    @Override
    protected Shape createShape() {
        URL[] urls = getImageUrls();
        try {
            Shape shape = null;

            for (URL url : urls) {
                FXMLLoader loader = new FXMLLoader(url);
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
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
}
