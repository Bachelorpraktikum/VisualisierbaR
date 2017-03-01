package com.github.bachelorpraktikum.dbvisualization.view.graph.adapter;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import javafx.geometry.Point2D;
import javax.annotation.Nonnull;

/**
 * An implementation of {@link CoordinatesAdapter} which does respect the real length of
 * Edges.
 */
public final class ProportionalCoordinatesAdapter implements CoordinatesAdapter {

    private double shortestEdgeLength;
    private Node startingNode;
    private Point2D startingPoint;
    private HashMap<Node, Point2D> transformationMap = new HashMap<>();

    public ProportionalCoordinatesAdapter(Context context) {
        shortestEdgeLength = Double.MAX_VALUE;

        // search for the shortest Edge
        for (Edge edge : Edge.in(context).getAll()) {
            double edgeLength = edge.getLength();

            if (edgeLength < shortestEdgeLength) {
                shortestEdgeLength = edgeLength;
            }
        }

        int x = Integer.MAX_VALUE;
        int y = Integer.MAX_VALUE;

        // search for a starting Node
        // this will be the Node with the smallest x and y coordinates,
        // which is the Node in the top left corner
        startingNode = Node.in(context).getAll().stream()
            .sorted((n1, n2) -> {
                Coordinates c1 = n1.getCoordinates();
                Coordinates c2 = n2.getCoordinates();
                if (c1.getX() == c2.getX()) {
                    return Integer.compare(c1.getY(), c2.getY());
                } else {
                    return Integer.compare(c1.getX(), c2.getY());
                }
            })
            .findFirst().orElseThrow(IllegalStateException::new);

        startingPoint = startingNode.getCoordinates().toPoint2D();

        // calculate all transformation Vectors
        this.dfs();
    }

    @Override
    public double getCalibrationBase() {
        return 2;
    }

    /**
     * Calculates a point for the given Node. This Point is the Place
     * the Node should be placed on the screen.
     *
     * @return the Point at which the given Node should be placed
     */
    @Nonnull
    @Override
    public Point2D apply(@Nonnull Node node) {
        Point2D transformVec = transformationMap.get(node);
        return startingPoint.add(transformVec);
    }

    /**
     * Use Depth-First Search to visit every node in the Graph
     */
    private void dfs() {
        Stack<Node> Q = new Stack<>();
        Set<Node> S = new HashSet<>();
        Q.push(startingNode);
        transformationMap.put(startingNode, startingPoint);

        while (!Q.isEmpty()) {
            Node current = Q.pop();

            if (!S.contains(current)) {
                S.add(current);
                for (Edge edge : current.getEdges()) {
                    if (edge.getNode1().equals(current)) {
                        processNode(edge.getNode2(), current, edge, Q, S);
                    } else {
                        processNode(edge.getNode1(), current, edge, Q, S);
                    }
                }
            }
        }
    }

    /**
     * Helper function for the dfs algorithm
     *
     * @param v a neighbour of u
     * @param u the currently processed node
     * @param edge the edge between u and v
     * @param Q the current set of nodes
     * @param S the set of already processed Nodes
     */
    private void processNode(Node v, Node u, Edge edge, Stack<Node> Q, Set<Node> S) {
        if (S.contains(v)) {
            return;
        }
        Point2D vCoord = v.getCoordinates().toPoint2D();
        Point2D uCoord = u.getCoordinates().toPoint2D();
        Point2D normVec = vCoord.subtract(uCoord).normalize();
        double scaleFactor = edge.getLength() / shortestEdgeLength;
        Point2D edgeVec = normVec.multiply(scaleFactor);
        Point2D uVec = transformationMap.get(u);

        // this vector is from the startingNode to the point where
        // the node v should be placed
        Point2D transformationVec = uVec.add(edgeVec);
        if (transformationMap.containsKey(v)) {
            transformationMap.replace(v, transformationVec);
        } else {
            transformationMap.put(v, transformationVec);
        }
        Q.push(v);
    }
}
