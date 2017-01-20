package com.github.bachelorpraktikum.dbvisualization.view.graph.adapter;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.sun.javafx.geom.Vec2d;

import java.util.ArrayList;
import java.util.HashMap;

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
    //private HashMap<Node, Node> prevMap = new HashMap<>();

    public ProportionalCoordinatesAdapter(Context context) {
        this.context = context;
        shortestEdgeLength = Double.MAX_VALUE;

        for(Edge edge: Edge.in(context).getAll()) {
            double edgeLength = edge.getLength();

            if(edgeLength < shortestEdgeLength)
                shortestEdgeLength = edgeLength;
        }

        int x = Integer.MAX_VALUE;
        int y = Integer.MAX_VALUE;

        for(Node node: Node.in(context).getAll()) {
            Coordinates c = node.getCoordinates();
            if(c.getX() < x || c.getY() < y) {
                startingNode = node;
                x = c.getX();
                y = c.getY();
            }
        }

        startingCoordinates = startingNode.getCoordinates();

        this.dijkstra();
    }

    @Override
    public double getCalibrationBase() {
        return 2;
    }

    @Nonnull
    @Override
    public Point2D apply(@Nonnull Node node) {
        Vec2d transformVec = transformationMap.get(node);
        return new Point2D(startingCoordinates.getX() + transformVec.x, startingCoordinates.getY() + transformVec.y);
    }

    private void dijkstra() {
        ArrayList<Node> Q = new ArrayList<>();

        for(Node v: Node.in(context).getAll()) {
            transformationMap.put(v, new Vec2d(Double.MAX_VALUE, Double.MAX_VALUE));
            //prevMap.put(v, null);
            Q.add(v);
        }

        transformationMap.replace(startingNode, new Vec2d(0, 0));

        while(!Q.isEmpty()) {
            // u should be vertex with the minimal distance
            Node u = null;
            Vec2d uVector = new Vec2d(Double.MAX_VALUE, Double.MAX_VALUE);
            for(Node node: Q) {
                if(vectorLength(transformationMap.get(node)) < vectorLength(uVector)) {
                    u = node;
                    uVector = transformationMap.get(node);
                }
            }

            // remove u from Q
            Q.remove(u);

            if(u == null)
                throw new IllegalStateException();

            for(Edge edge: u.getEdges()) {
                if(edge.getNode1().equals(u)) {
                    processNode(edge.getNode2(), u, edge);
                } else {
                    processNode(edge.getNode1(), u, edge);
                }
            }
        }
    }

    private void processNode(Node node, Node u, Edge edge) {
        Coordinates nodeCoord = node.getCoordinates();
        Coordinates uCoord = u.getCoordinates();
        Vec2d normVec = uCoord.normVectorTo(nodeCoord);
        double scaleFactor = edge.getLength() / shortestEdgeLength;
        Vec2d transformVec = new Vec2d(normVec.x * scaleFactor, normVec.y * scaleFactor);
        Vec2d uVec = transformationMap.get(u);
        Vec2d alt = new Vec2d(uVec.x + transformVec.x, uVec.y + transformVec.y);
        if(vectorLength(alt) < vectorLength(transformationMap.get(node))) {
            transformationMap.replace(node, alt);
            //prevMap.replace(node, u);
        }
    }

    private double vectorLength(Vec2d v) {
        return Math.sqrt(v.x*v.x + v.y*v.y);
    }
}
