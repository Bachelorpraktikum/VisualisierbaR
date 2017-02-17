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
import javax.annotation.Nonnull;

final class CompositeElement extends ElementBase<Group> {

    private static final double MAX_SHAPE_WIDTH = 2.0;
    private static final double ELEMENT_SPACING = 0.7;
    private static final double GESCHWINDIGKEITS_ANZEIGER_WIDTH_FACTOR = 0.5;
    private static final double FOOT_HEIGHT = 0.5;

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
            shape.relocate(0 - bounds.getWidth() / 2, 0 - bounds.getHeight() / 2);
            shape.setTranslateY(y.doubleValue());
            y.add(shape.getBoundsInParent().getHeight() + ELEMENT_SPACING);
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
        resizeNode(shape, MAX_ELEMENT_WIDTH * getCalibrationBase());
    }

    private void resizeNode(javafx.scene.Node node, double maxWidth) {
        Bounds bounds = node.getLayoutBounds();
        double factor = maxWidth / bounds.getWidth();
        node.setScaleX(node.getScaleX() * factor);
        node.setScaleY(node.getScaleY() * factor);
    }

    @Override
    public Shape getShape(Element represented) {
        return shapes.get(represented);
    }

    @Nonnull
    @Override
    protected Group createShape() {
        Group group = new Group(shapes.values().stream()
            .collect(Collectors.toList())
        );
        Bounds bounds = group.getLayoutBounds();
        double endY = bounds.getHeight() + FOOT_HEIGHT * getCalibrationBase();
        Line line = new Line(0, 0, 0, endY);
        line.setStrokeWidth(0.16 * getCalibrationBase());
        group.getChildren().add(line);
        line.toBack();

        bounds = group.getLayoutBounds();
        double x = bounds.getWidth() / 2;
        Line bottomLine = new Line(-x, endY, x, endY);
        bottomLine.setStrokeWidth(0.16 * getCalibrationBase());
        group.getChildren().add(bottomLine);
        bottomLine.toBack();
        return group;
    }

    private Shape createShape(Element.Type type) {
        double maxWidth = MAX_SHAPE_WIDTH;
        Shape shape = null;
        switch (type) {
            case VorSignalImpl:
            case HauptSignalImpl:
                shape = createPathShape(type);
                break;
            case GeschwindigkeitsAnzeigerImpl:
                shape = new Polygon(0, 2, 1, 0, 2, 2);
                maxWidth *= GESCHWINDIGKEITS_ANZEIGER_WIDTH_FACTOR;
                break;
            case GeschwindigkeitsVoranzeiger:
                shape = new Polygon(0, 0, 1, 2, 2, 0);
                maxWidth *= GESCHWINDIGKEITS_ANZEIGER_WIDTH_FACTOR;
                break;
            case UnknownElement:
            default:
                shape = new Rectangle(2, 2);
                break;
        }
        resizeNode(shape, getCalibrationBase() * maxWidth);
        return shape;
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
            return shape;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }
}
