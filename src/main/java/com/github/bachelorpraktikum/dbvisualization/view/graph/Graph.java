package com.github.bachelorpraktikum.dbvisualization.view.graph;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
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

import javafx.scene.Group;

@ParametersAreNonnullByDefault
public final class Graph {
    @Nonnull
    private final Context context;
    @Nonnull
    private final CoordinatesAdapter coordinatesAdapter;

    @Nonnull
    private final Group group;

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
        this.nodes = new LinkedHashMap<>(128);
        this.elements = new LinkedHashMap<>(256);
        this.group = new Group();
        for (Node node : Node.in(context).getAll()) {
            GraphShape<Node> shape = new Junction(node, coordinatesAdapter);
            nodes.put(node, shape);
            group.getChildren().add(shape.getShape());

            for (GraphShape<Element> elementShape : Elements.create(node, coordinatesAdapter)) {
                for (Element element : elementShape.getRepresentedObjects()) {
                    elements.put(element, elementShape);
                }
                group.getChildren().add(elementShape.getShape());
            }
        }

        this.edges = new LinkedHashMap<>(256);
        for (Edge edge : Edge.in(context).getAll()) {
            GraphShape<Edge> shape = new Rail(edge, coordinatesAdapter);
            edges.put(edge, shape);
            group.getChildren().add(shape.getShape());
        }
    }

    public void scale(double factor) {
        double scale = group.getScaleX() * factor;
        group.setScaleX(scale);
        group.setScaleY(scale);
    }

    public void move(double x, double y) {
        group.setTranslateX(group.getTranslateX() + x);
        group.setTranslateY(group.getTranslateY() + y);
    }

    public Group getGroup() {
        return group;
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
