package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.shape.Shape;
import javax.annotation.Nonnull;

class PathElement extends SingleElementBase<Shape> {

    PathElement(Element element, Node node, CoordinatesAdapter adapter) {
        super(element, node, adapter);
    }

    @Override
    protected void relocate(Shape shape) {
        Point2D nodePos = getNodePosition().add(getOffset());

        Bounds bounds = shape.getBoundsInLocal();
        double x = nodePos.getX() - (bounds.getWidth()) / 2;
        double y = nodePos.getY() - bounds.getHeight() / 2;

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

    protected List<URL> getImageUrls() {
        return getElement().getType().getImageUrls();
    }

    @Nonnull
    @Override
    protected Shape createShape() {
        try {
            Shape shape = null;

            for (URL url : getImageUrls()) {
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
