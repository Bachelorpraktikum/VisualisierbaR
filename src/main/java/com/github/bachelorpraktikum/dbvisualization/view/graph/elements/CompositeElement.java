package com.github.bachelorpraktikum.dbvisualization.view.graph.elements;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

final class CompositeElement extends ElementBase<Group> {

    private final Map<Element, Shape> shapes;
    private final List<ChangeListener<Element.State>> stateListeners;
    private final int count;

    CompositeElement(List<Element> elements, Node node, CoordinatesAdapter adapter, int count) {
        super(elements, node, adapter);
        this.count = count;
        this.stateListeners = new ArrayList<>(elements.size());
        this.shapes = new LinkedHashMap<>();

        DoubleAdder y = new DoubleAdder();
        elements.stream().sorted(Comparator.comparing(Element::getType)).forEach(element -> {
            Shape shape = createShape(element.getType());
            Bounds bounds = shape.getLayoutBounds();
            shape.relocate(0 - bounds.getWidth() / 2, y.doubleValue() - bounds.getHeight() / 2);
            y.add(shape.getLayoutBounds().getHeight());
            ChangeListener<Element.State> listener = (observable, oldValue, newValue) -> {
                shape.setFill(newValue.getColor());
            };
            stateListeners.add(listener);
            element.stateProperty().addListener(new WeakChangeListener<>(listener));
            shapes.put(element, shape);
        });
    }

    @Override
    protected Point2D getOffset() {
        Point2D offset = super.getOffset();
        offset = offset.add(offset.multiply(count));
        return offset;
    }

    @Override
    protected void relocate(Group group) {
        Point2D nodePos = getNodePosition().add(getOffset());

        Bounds bounds = group.getBoundsInLocal();
        double x = nodePos.getX() - bounds.getWidth() / 2;
        double y = nodePos.getY() - bounds.getHeight() / 2;

        group.relocate(x, y);
        rotateAccordingToOffset(group);
    }

    @Override
    protected void resize(Group shape) {
        resizeNode(shape, 0.3);
    }

    private void resizeNode(javafx.scene.Node node, double maxSize) {
        Bounds bounds = node.getLayoutBounds();
        double f = maxSize / bounds.getWidth();
        node.setScaleX(node.getScaleX() * f);
        node.setScaleY(node.getScaleY() * f);
    }

    @Override
    public Shape getShape(Element represented) {
        return shapes.get(represented);
    }

    private Shape createShape(Element.Type type) {
        switch (type) {
            case HauptSignalImpl:
                return createPathShape(type);
            case VorSignalImpl:
                return createPathShape(type);
            case GeschwindigkeitsAnzeigerImpl:
                Polygon polygon = new Polygon(0, 2, 1, 0, 2, 2);
                resizeNode(polygon, 1.0);
                return polygon;
            case UnknownElement:
                Rectangle rectangle = new Rectangle(2, 2);
                resizeNode(rectangle, 1.0);
                return rectangle;
            default:
                return new Rectangle(2, 2);
        }
    }

    private Shape createPathShape(Element.Type type) {
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

            shape.setRotate(90);
            resizeNode(shape, 2);
            return shape;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Nonnull
    @Override
    protected Group createShape() {
        Group group = new Group(shapes.values().stream()
                .collect(Collectors.toList()));
        Bounds bounds = group.getLayoutBounds();
        double endY = bounds.getHeight() + 0.5;
        Line line = new Line(0, 0, 0, endY);
        line.setStrokeWidth(0.16);
        group.getChildren().add(line);
        line.toBack();

        bounds = group.getLayoutBounds();
        double x = bounds.getWidth() / 2;
        Line bottomLine = new Line(-x, endY, x, endY);
        bottomLine.setStrokeWidth(0.16);
        group.getChildren().add(bottomLine);
        bottomLine.toBack();
        return group;
    }
}
