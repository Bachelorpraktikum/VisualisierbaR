package com.github.bachelorpraktikum.dbvisualization.view.graph.adapter;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.sun.javafx.geom.Vec2d;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;

import javax.annotation.Nonnull;

import javafx.geometry.Point2D;

/**
 * An implementation of {@link CoordinatesAdapter} which does respect the real length of
 * Edges.
 */
public final class ProportionalCoordinatesAdapter implements CoordinatesAdapter {
    private double shortestEdgeLength;
    private Node startingNode;
    private Coordinates startingCoordinates;
    private Context context;
    private HashMap<Node, Vec2d> transformationMap = new HashMap<>();

    public ProportionalCoordinatesAdapter(Context context) {
        this.context = context;
        shortestEdgeLength = Double.MAX_VALUE;

        // search for the shortest Edge
        for(Edge edge: Edge.in(context).getAll()) {
            double edgeLength = edge.getLength();

            if(edgeLength < shortestEdgeLength)
                shortestEdgeLength = edgeLength;
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

        startingCoordinates = startingNode.getCoordinates();

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
        Vec2d transformVec = transformationMap.get(node);
        return new Point2D(startingCoordinates.getX() + transformVec.x, startingCoordinates.getY() + transformVec.y);
    }

    /**
     * Use Depth-First Search to visit every node in the Graph
     */
    private void dfs() {
        Stack<Node> Q = new Stack<>();
        Set<Node> S = new LinkedHashSet<>();
        Q.push(startingNode);
        transformationMap.put(startingNode, new Vec2d(startingCoordinates.getX(), startingCoordinates.getY()));

        while(!Q.isEmpty()) {
            Node current = Q.pop();

            if(!S.contains(current)) {
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
     *
     * @param v a neighbour of u
     * @param u the currently processed node
     * @param edge the edge between u and v
     * @param Q the current set of nodes
     * @param S the set of already processed Nodes
     */
    private void processNode(Node v, Node u, Edge edge, Stack<Node> Q, Set<Node> S) {
        if(S.contains(v))
            return;
        Coordinates vCoord = v.getCoordinates();
        Coordinates uCoord = u.getCoordinates();
        Vec2d normVec = uCoord.normVectorTo(vCoord);
        double scaleFactor = edge.getLength() / shortestEdgeLength;
        Vec2d edgeVec = new Vec2d(normVec.x * scaleFactor, normVec.y * scaleFactor);
        Vec2d uVec = transformationMap.get(u);

        // this vector is from the startingNode to the point where
        // the node v should be placed
        Vec2d transformationVec = new Vec2d(uVec.x + edgeVec.x, uVec.y + edgeVec.y);
        if(transformationMap.containsKey(v))
            transformationMap.replace(v, transformationVec);
        else
            transformationMap.put(v, transformationVec);
        Q.push(v);
    }
}
