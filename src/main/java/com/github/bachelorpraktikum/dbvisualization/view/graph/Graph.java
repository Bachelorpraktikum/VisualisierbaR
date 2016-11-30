package com.github.bachelorpraktikum.dbvisualization.view.graph;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import javafx.geometry.Point2D;
import javafx.scene.shape.Shape;

@ParametersAreNonnullByDefault
public final class Graph implements Shapeable {
    @Nonnull
    private final Context context;
    @Nonnull
    private final CoordinatesAdapter coordinatesAdapter;

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
    }

    @Nonnull
    @Override
    public Shape createShape() {
        Map<Node, NodeShape> nodes = Node.in(context).getAll().parallelStream()
                .collect(Collectors.toMap(Function.identity(),
                        node -> new NodeShape(coordinatesAdapter, node)));

        double calibrationBase = coordinatesAdapter.getCalibrationBase();
        Shape rails = Edge.in(context).getAll().parallelStream()
                .map(edge -> {
                    Point2D start = nodes.get(edge.getNode1()).getPosition();
                    Point2D end = nodes.get(edge.getNode2()).getPosition();
                    return new Rail(calibrationBase, start, end);
                })
                .map(Shapeable::createShape)
                .reduce(Shape::union)
                .orElseThrow(IllegalStateException::new);

        return Shape.union(rails,
                nodes.values().parallelStream()
                        .map(NodeShape::createShape)
                        .reduce(Shape::union)
                        .orElseThrow(IllegalStateException::new)
        );
    }
}
