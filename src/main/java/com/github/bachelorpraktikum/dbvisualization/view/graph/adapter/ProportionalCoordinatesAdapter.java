package com.github.bachelorpraktikum.dbvisualization.view.graph.adapter;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javafx.geometry.Point2D;
import javax.annotation.Nonnull;

/**
 * An implementation of {@link CoordinatesAdapter} which does respect the real length of
 * Edges.
 */
public final class ProportionalCoordinatesAdapter implements CoordinatesAdapter {

    private Context context;
    private double shortestEdgeLength;
    private Node startingNode;
    private Point2D startingPoint;
    private HashMap<Node, Point2D> transformationMap = new HashMap<>();
    private LinkedList<GraphSegment> segments = new LinkedList<>();
    private final static double MOVING_DISTANCE = 1.5;

    public ProportionalCoordinatesAdapter(Context context) {
        this.context = context;
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
        // detect straight graph segments
        this.detectSegments();
        // try to move these segments to avoid overlapping
        this.removeSegmentCollisions();
        // moves single nodes to avoid collisions
        this.removeSingleNodeCollisions();
    }

    /**
     * Finds and fixes Nodes overlapping with other Edges
     *
     * @return true if a collision was found and fixed, false otherwise.
     */
    private boolean removeSingleNodeCollisions() {
        boolean foundCollisions = false;

        // first find overlapping nodes and replace their transformation vectors
        for(Map.Entry<Node, Point2D> entry: transformationMap.entrySet()) {
            for(Map.Entry<Node, Point2D> entry2: transformationMap.entrySet()) {
                if(entry.getValue().equals(entry2.getValue()) &&
                        !entry.getKey().equals(entry2.getKey())) {
                    // same transformation Vector but different Nodes
                    Node node = entry2.getKey();
                    Edge edge = null;
                    Point2D edgeVec = null;
                    // look for an edge that will serve as a vector to move the node
                    for(Edge e: node.getEdges()) {
                        double p1X = this.apply(e.getNode1()).getX();
                        double p1Y = this.apply(e.getNode1()).getY();
                        double p2X = this.apply(e.getNode2()).getX();
                        double p2Y = this.apply(e.getNode2()).getY();
                        double dx = (p1X > p2X) ? p2X - p1X : p1X - p2X;
                        double dy = (p1Y > p2Y) ? p2Y - p1Y : p1Y - p2Y;
                        Point2D eVec = new Point2D(dx, dy);

                        // prefer vertical edges
                        if(eVec.getY() == 0) {
                            edge = e;
                            edgeVec = eVec.normalize();
                            continue;
                        }

                        // horizontal edges used if no better edge is found
                        if(eVec.getY() == 0 && edge == null) {
                            edge = e;
                            edgeVec = eVec.normalize();
                            continue;
                        }

                        edge = e;
                        edgeVec = eVec.normalize();
                    }

                    Point2D oldVec = entry2.getValue();
                    Point2D newVec = oldVec.add(edgeVec.multiply(MOVING_DISTANCE));
                    transformationMap.replace(node, newVec);

                    foundCollisions = true;
                }
            }
        }

        // find nodes that lie on other edges and move them
        for(Node node: Node.in(context).getAll()) {
            Point2D nodePoint = this.apply(node);

            for(Edge edge: Edge.in(context).getAll()) {
                // check if the current node should be part
                // of the current edge and continue with another edge
                // if that is the case
                boolean isNodeEdge = false;
                for(Edge nodeEdge: node.getEdges()) {
                    if(nodeEdge.equals(edge))
                        isNodeEdge = true;
                }
                if(isNodeEdge)
                    continue;

                Point2D edgeP1 = this.apply(edge.getNode1());
                Point2D edgeP2 = this.apply(edge.getNode2());
                double dx = edgeP2.getX() - edgeP1.getX();
                double dy = edgeP2.getY() - edgeP1.getY();
                Point2D edgeNormal = new Point2D(-dy, dx).normalize();

                // find the distance of the nodePoint (P) from
                // the edge end points (A, B).
                double AB = edgeP1.distance(edgeP2);
                double AP = edgeP1.distance(nodePoint);
                double BP = edgeP2.distance(nodePoint);

                if(AB == AP + BP) {
                    // nodePoint lies on the edge
                    Point2D oldVec = transformationMap.get(node);
                    // move the node along the normal of the edge
                    Point2D newVec = oldVec.add(edgeNormal.multiply(MOVING_DISTANCE));
                    transformationMap.replace(node, newVec);
                    foundCollisions = true;
                }
            }
        }

        return foundCollisions;
    }

    /**
     * Tries to move segments of straight edges to remove overlapping nodes
     * and edges.
     */
    private void removeSegmentCollisions() {
        for(int i=0; i < segments.size(); i++) {
            for(int j=i+1; j < segments.size(); j++) {
                GraphSegment gs = segments.get(i);
                GraphSegment gs2 = segments.get(j);

                // no need to compare segments that are not aligned
                if(!gs2.isSameTypeAs(gs))
                    continue;

                if(gs.checkForCollision(gs2)) {
                    // collision
                    if(gs.getSize() > gs2.getSize())
                        moveNodes(gs2);
                    else
                        moveNodes(gs);
                }
            }
        }
    }

    /**
     * Moves all Nodes of the given GraphSegment along the normal vector
     * of that segment.
     *
     * @param gs segment that will be moved
     */
    private void moveNodes(GraphSegment gs) {
        Point2D normalVector = gs.getNormalVector();

        for(Node n: gs.getNodes()) {
            Point2D oldVec = transformationMap.get(n);
            Point2D newVec = oldVec.add(normalVector.multiply(MOVING_DISTANCE));
            transformationMap.replace(n, newVec);
        }
    }

    /**
     * Detects segments of consecutive straight edges (only axis-aligned).
     */
    private void detectSegments() {
        Stack<Node> Q = new Stack<>();
        Set<Node> S = new HashSet<>();
        Q.push(startingNode);
        GraphSegment.setAdapter(this);
        GraphSegment currentSegment = null;

        while (!Q.isEmpty()) {
            Node current = Q.pop();
            Node next;

            if (!S.contains(current)) {
                S.add(current);
                for (Edge edge : current.getEdges()) {
                    // choose the right Node
                    if (edge.getNode1().equals(current)) {
                        next = edge.getNode2();
                    } else {
                        next = edge.getNode1();
                    }

                    // if node was already seen skip it
                    if(S.contains(next))
                        continue;

                    // process the node
                    Point2D currentPoint = this.apply(current);
                    Point2D nextPoint = this.apply(next);

                    Point2D cnVector = nextPoint.subtract(currentPoint);

                    if(cnVector.getY() == 0) {
                        // straight horizontal edge
                        if(currentSegment == null) {
                            currentSegment = new GraphSegment(SegmentType.HORIZONTAL);
                            currentSegment.addNode(current);
                            currentSegment.addNode(next);
                        } else if(currentSegment.getSegmentType() == SegmentType.HORIZONTAL) {
                            currentSegment.addNode(next);
                        } else {
                            currentSegment.endSegment();
                            this.segments.add(currentSegment);
                            currentSegment = new GraphSegment(SegmentType.HORIZONTAL);
                            currentSegment.addNode(current);
                            currentSegment.addNode(next);
                        }
                    } else if(cnVector.getX() == 0) {
                        // straight vertical edge
                        if(currentSegment == null) {
                            currentSegment = new GraphSegment(SegmentType.VERTICAL);
                            currentSegment.addNode(current);
                            currentSegment.addNode(next);
                        } else if(currentSegment.getSegmentType() == SegmentType.VERTICAL) {
                            currentSegment.addNode(next);
                        } else {
                            currentSegment.endSegment();
                            this.segments.add(currentSegment);
                            currentSegment = new GraphSegment(SegmentType.VERTICAL);
                            currentSegment.addNode(current);
                            currentSegment.addNode(next);
                        }
                    } else {
                        // no straight edge anymore
                        // end the segment
                        if(currentSegment != null) {
                            currentSegment.endSegment();
                            this.segments.add(currentSegment);
                            currentSegment = null;
                        }
                    }

                    // add node to stack
                    Q.push(next);
                }
            }
        }
        // add last segment after queue is empty
        if(currentSegment != null) {
            currentSegment.endSegment();
            this.segments.add(currentSegment);
        }
    }

    @Override
    public double getCalibrationBase() {
        return 2.7;
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
