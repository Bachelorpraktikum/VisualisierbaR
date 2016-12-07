package com.github.bachelorpraktikum.dbvisualization.view.graph;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;
import com.github.bachelorpraktikum.dbvisualization.view.graph.elements.ElementBase;
import com.github.bachelorpraktikum.dbvisualization.view.graph.elements.Elements;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Transform;

@ParametersAreNonnullByDefault
public final class Graph {
    @Nonnull
    private final Context context;
    @Nonnull
    private final CoordinatesAdapter coordinatesAdapter;

    @Nonnull
    private final Shape boundsShape;
    @Nonnull
    private final ReadOnlyObjectWrapper<Transform> transformProperty;

    @Nonnull
    private final Map<Node, GraphShape> nodes;
    @Nonnull
    private final Map<Edge, GraphShape> edges;
    @Nonnull
    private final Map<Element, GraphShape> elements;


    /**
     * Creates a new graph for the given context. The graph is laid out by using the given {@link
     * CoordinatesAdapter}.
     *
     * @param context            the context
     * @param coordinatesAdapter the coordinates adapter to translate coordinates from the model to
     *                           real coordinates
     * @throws NullPointerException  if either argument is null
     * @throws IllegalStateException if there is nothing for this context to show
     */
    public Graph(Context context, CoordinatesAdapter coordinatesAdapter) {
        this.context = Objects.requireNonNull(context);
        this.coordinatesAdapter = Objects.requireNonNull(coordinatesAdapter);
        this.transformProperty = new ReadOnlyObjectWrapper<>();

        boundsShape = Node.in(context).getAll().parallelStream()
                .map(Node::getCoordinates)
                .map(coordinatesAdapter)
                .map(point -> (Shape) new Circle(point.getX(), point.getY(), 0.7))
                .reduce(Shape::union)
                .orElseThrow(IllegalStateException::new);
        transformProperty.bind(boundsShape.localToParentTransformProperty());

        this.nodes = new LinkedHashMap<>(128);
        this.elements = new LinkedHashMap<>(256);
        for (Node node : Node.in(context).getAll()) {
            GraphShape shape = new Junction(node, transformProperty, coordinatesAdapter);
            nodes.put(node, shape);

            for (ElementBase elementShape : Elements.create(node, transformProperty, coordinatesAdapter)) {
                elements.put(elementShape.getElement(), elementShape);
            }
        }

        this.edges = new LinkedHashMap<>(256);
        for (Edge edge : Edge.in(context).getAll()) {
            GraphShape shape = new Rail(edge, transformProperty, coordinatesAdapter);
            edges.put(edge, shape);
        }
    }

    public void scale(double factor) {
        double scale = boundsShape.getScaleX() * factor;
        boundsShape.setScaleX(scale);
        boundsShape.setScaleY(scale);
    }

    public void move(double x, double y) {
        boundsShape.setTranslateX(boundsShape.getTranslateX() + x);
        boundsShape.setTranslateY(boundsShape.getTranslateY() + y);
    }

    public Point2D getPosition(Point2D localPosition) {
        return transformProperty.getValue().transform(localPosition);
    }

    public Point2D getPosition(Coordinates coordinates) {
        return getPosition(coordinatesAdapter.apply(coordinates));
    }

    public Bounds getBounds() {
        return boundsShape.getBoundsInParent();
    }

    public Map<Node, GraphShape> getNodes() {
        return nodes;
    }

    public Map<Edge, GraphShape> getEdges() {
        return edges;
    }

    public Map<Element, GraphShape> getElements() {
        return elements;
    }
}
