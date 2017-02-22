package com.github.bachelorpraktikum.dbvisualization.view.graph;

import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class GraphShapeBase<R, S extends Node> implements GraphShape<R> {

    protected static final double HIGHLIGHT_FACTOR = 1.5;
    protected static final double HIGHLIGHT_STROKE_WIDTH = 0.05;
    private final CoordinatesAdapter adapter;
    private final BooleanProperty highlighted = new SimpleBooleanProperty();

    @Nullable
    private S shape;
    @Nullable
    private Node highlight;
    @Nullable
    private Group full;


    protected GraphShapeBase(CoordinatesAdapter adapter) {
        this.adapter = adapter;
    }

    protected S initializeShape() {
        S shape = createShape();
        resize(shape);
        relocate(shape);
        // shape.setOnMouseClicked(event -> System.out.println("CLICK: " + getRepresented()));
        return shape;
    }

    protected void initializedShape(S shape) {
    }

    @Override
    public final S getShape() {
        if (shape == null) {
            shape = initializeShape();
            initializedShape(shape);
            highlight = createHighlight(shape);
            highlight.visibleProperty().bind(highlightedProperty());
        }

        return shape;
    }

    @Override
    public Node getFullNode() {
        if (full == null) {
            full = new Group();
            full.getChildren().addAll(getShape(), highlight);
        }
        return full;
    }

    @Override
    public BooleanProperty highlightedProperty() {
        return highlighted;
    }

    protected final CoordinatesAdapter getCoordinatesAdapter() {
        return adapter;
    }

    protected final double getCalibrationBase() {
        return getCoordinatesAdapter().getCalibrationBase();
    }

    protected Point2D getOffset() {
        return new Point2D(0.4, 0.4).multiply(getCalibrationBase());
    }

    protected abstract void relocate(S shape);

    protected abstract void resize(S shape);

    @Nonnull
    protected abstract S createShape();

    protected abstract Node createHighlight(S node);

    protected Node createCircleHighlight(Node node) {
        Circle circle = new Circle();
        Bounds nodeBounds = node.getBoundsInParent();
        circle.setCenterY(nodeBounds.getMinY() + nodeBounds.getHeight() / 2);
        circle.setCenterX(nodeBounds.getMinX() + nodeBounds.getWidth() / 2);
        circle.setRadius(
            Math.max(nodeBounds.getWidth(), nodeBounds.getHeight()) * HIGHLIGHT_FACTOR
        );

        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.BLUE);
        circle.setStrokeWidth(
            HIGHLIGHT_STROKE_WIDTH * getCalibrationBase()
        );
        return circle;
    }

    protected Node createRectangleHighlight(Node node) {
        Rectangle rectangle = new Rectangle();
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setStroke(Color.BLUE);
        rectangle.setStrokeWidth(HIGHLIGHT_STROKE_WIDTH * getCalibrationBase());
        Bounds nodeBounds = node.getBoundsInParent();
        double width = nodeBounds.getWidth() * HIGHLIGHT_FACTOR;
        double height = nodeBounds.getHeight() * HIGHLIGHT_FACTOR;
        rectangle.setX(nodeBounds.getMinX() - (width - nodeBounds.getWidth()) / 2);
        rectangle.setY(nodeBounds.getMinY() - (height - nodeBounds.getHeight()) / 2);
        rectangle.setWidth(width);
        rectangle.setHeight(height);
        return rectangle;
    }
}
