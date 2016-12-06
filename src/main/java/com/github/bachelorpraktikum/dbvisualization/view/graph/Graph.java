package com.github.bachelorpraktikum.dbvisualization.view.graph;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.shape.Shape;

@ParametersAreNonnullByDefault
public final class Graph implements Shapeable {
    @Nonnull
    private final Context context;
    @Nonnull
    private final CoordinatesAdapter coordinatesAdapter;
    @Nonnull
    private final Map<Node, NodeShape> nodes;
    @Nonnull
    private final Map<Edge, Rail> edges;

    @Nullable
    private Shape railsShape;
    @Nullable
    private Shape nodesShape;
    @Nullable
    private Shape fullShape;

    /**
     * Creates a new graph for the given context. The graph is laid out by using the given {@link
     * CoordinatesAdapter}.
     *
     * @param context            the context
     * @param coordinatesAdapter the coordinates adapter to translate coordinates from the model to
     *                           real coordinates
     * @throws NullPointerException if either argument is null
     */
    public Graph(Context context, CoordinatesAdapter coordinatesAdapter) {
        this.context = Objects.requireNonNull(context);
        this.coordinatesAdapter = Objects.requireNonNull(coordinatesAdapter);

        ChangeListener<Element.State> elementListener = new WeakChangeListener<>(
                (observable, oldValue, newValue) -> {
                    nodesShape = null;
                    fullShape = null;
                }
        );
        Element.in(context).getAll().parallelStream()
                .forEach(element -> element.stateProperty().addListener(elementListener));

        this.nodes = Node.in(context).getAll().parallelStream()
                .collect(Collectors.toMap(Function.identity(),
                        node -> new NodeShape(coordinatesAdapter, node)));

        double calibrationBase = coordinatesAdapter.getCalibrationBase();
        this.edges = Edge.in(context).getAll().parallelStream()
                .collect(Collectors.toMap(Function.identity(),
                        edge -> {
                            Point2D start = nodes.get(edge.getNode1()).getPosition();
                            Point2D end = nodes.get(edge.getNode2()).getPosition();
                            return new Rail(calibrationBase, start, end);
                        })
                );
    }

    @Nonnull
    @Override
    public Shape createShape() {
        if (nodesShape != null && railsShape != null) {
            if (fullShape == null) {
                throw new IllegalStateException("full shape was null for some reason");
            }
            return fullShape;
        }

        if (nodesShape == null) {
            nodesShape = nodes.values().parallelStream()
                    .map(Shapeable::createShape)
                    .reduce(Shape::union)
                    .orElseThrow(IllegalStateException::new);
        }

        if (railsShape == null) {
            railsShape = edges.values().parallelStream()
                    .map(Shapeable::createShape)
                    .reduce(Shape::union)
                    .orElseThrow(IllegalStateException::new);
        }

        return fullShape = Shape.union(nodesShape, railsShape);
    }
}
