package com.github.bachelorpraktikum.dbvisualization.view.graph;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;
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
    private final Map<Node, GraphShape<Node>> nodes;
    @Nonnull
    private final Map<Edge, GraphShape<Edge>> edges;
    @Nonnull
    private final Map<Element, GraphShape<Element>> elements;


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

        Shape boundsShape = Node.in(context).getAll().parallelStream()
                .map(Node::getCoordinates)
                .map(coordinatesAdapter)
                .map(point -> (Shape) new Circle(point.getX(), point.getY(), 0.7))
                .reduce(Shape::union)
                .orElseThrow(IllegalStateException::new);

        // Temporary hack to leave extra space around the graph
        Bounds bounds = boundsShape.getBoundsInLocal();
        Circle up = new Circle(0, bounds.getMinY(), 0.7);
        Circle down = new Circle(0, bounds.getMaxY(), 0.7);
        Circle left = new Circle(bounds.getMinX(), 0, 0.7);
        Circle right = new Circle(bounds.getMaxX(), 0, 0.7);
        boundsShape = Shape.union(left, Shape.union(right, Shape.union(down, Shape.union(boundsShape, up))));
        this.boundsShape = boundsShape;

        transformProperty.bind(boundsShape.localToParentTransformProperty());

        this.nodes = new LinkedHashMap<>(128);
        this.elements = new LinkedHashMap<>(256);
        for (Node node : Node.in(context).getAll()) {
            GraphShape<Node> shape = new Junction(node, transformProperty, coordinatesAdapter);
            nodes.put(node, shape);

            for (GraphShape<Element> elementShape : Elements.create(node, transformProperty, coordinatesAdapter)) {
                elements.put(elementShape.getRepresented(), elementShape);
            }
        }

        this.edges = new LinkedHashMap<>(256);
        for (Edge edge : Edge.in(context).getAll()) {
            GraphShape<Edge> shape = new Rail(edge, transformProperty, coordinatesAdapter);
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

    public Map<Node, GraphShape<Node>> getNodes() {
        return nodes;
    }

    public Map<Edge, GraphShape<Edge>> getEdges() {
        return edges;
    }

    public Map<Element, GraphShape<Element>> getElements() {
        return elements;
    }
}
