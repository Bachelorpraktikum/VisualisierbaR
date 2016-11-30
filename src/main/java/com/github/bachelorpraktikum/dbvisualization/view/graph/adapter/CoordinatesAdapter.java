package com.github.bachelorpraktikum.dbvisualization.view.graph.adapter;

import com.github.bachelorpraktikum.dbvisualization.model.Node;

import java.util.function.Function;

import javax.annotation.Nonnull;

import javafx.geometry.Point2D;

/**
 * Represents an adapter for transforming the abstract coordinates of the model package to real
 * coordinates for visualization.
 */
public interface CoordinatesAdapter extends Function<Node, Point2D> {
    /**
     * Gets the length of the shortest edge in the graph. Should be used to determine the size of
     * elements in the graph, like the size of nodes or the width of edges.<br> The length returned
     * by this method is the real length corresponding to the real coordinates returned by {@link
     * #apply(Node)}.
     *
     * @return the length of the shortest edge
     */
    double getCalibrationBase();

    /**
     * Gets the real position of this node.
     *
     * @param node the node of which the coordinates should be transformed
     * @throws NullPointerException if node is null
     */
    @Nonnull
    @Override
    Point2D apply(@Nonnull Node node);
}
