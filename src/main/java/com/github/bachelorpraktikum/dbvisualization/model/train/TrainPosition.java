package com.github.bachelorpraktikum.dbvisualization.model.train;

import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javafx.geometry.Point2D;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

@Immutable
@ParametersAreNonnullByDefault
final class TrainPosition implements Train.Position {

    private final Train train;
    private final LinkedList<Edge> edges;
    private final Node frontNode;
    private final Node unreachedFrontNode;
    private final int frontDistance;
    private final Node backNode;
    private final Node unreachedBackNode;
    private final int backDistance;

    private TrainPosition(Train train,
        LinkedList<Edge> edges,
        int frontDistance,
        Node frontNode,
        Node unreachedFrontNode,
        int backDistance,
        Node backNode,
        Node unreachedBackNode) {
        this.train = train;
        this.edges = edges;
        this.frontDistance = frontDistance;
        this.frontNode = frontNode;
        this.unreachedFrontNode = unreachedFrontNode;
        this.backDistance = backDistance;
        this.backNode = backNode;
        this.unreachedBackNode = unreachedBackNode;
    }

    /**
     * <p>Creates a TrainPosition as if the train was initialized on the given edge.</p>
     *
     * @param train the train this position belongs to
     * @param edge the edge the train was initialized on
     * @param start the node the front is coming from
     * @param end the node the front is trying to reach
     * @return a TrainPosition
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if the edge doesn't connect the nodes
     * @see Train.EventFactory#init(int, Edge)
     */
    @Nonnull
    static TrainPosition init(Train train, Edge edge, Node start, Node end) {
        Objects.requireNonNull(train);
        Objects.requireNonNull(edge);
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);

        if (!edge.getOtherNode(start).equals(end)) {
            throw new IllegalArgumentException("end node is not on edge");
        }

        LinkedList<Edge> edges = new LinkedList<>();
        edges.add(edge);

        return new TrainPosition(train,
            edges,
            Math.min(train.getLength(), edge.getLength()),
            start,
            end,
            edge.getLength(),
            end,
            start);
    }

    @Nonnull
    @Override
    public Train getTrain() {
        return train;
    }

    @Nonnull
    @Override
    public Edge getFrontEdge() {
        return edges.getFirst();
    }

    @Override
    public int getFrontDistance() {
        return frontDistance;
    }

    @Nonnull
    @Override
    public Point2D getFrontCoordinates() {
        return getEndCoordinates(frontNode, unreachedFrontNode, getFrontEdge(), frontDistance);
    }

    @Nonnull
    @Override
    public Point2D getFrontPosition(Function<Node, Point2D> adapter) {
        Point2D front = adapter.apply(frontNode);
        Point2D unreached = adapter.apply(unreachedFrontNode);
        Point2D vector = getVector(front, unreached);
        double length =
            ((double) getFrontDistance()) / getFrontEdge().getLength() * vector.magnitude();
        return front.add(vector.normalize().multiply(length));
    }

    @Nonnull
    @Override
    public Edge getBackEdge() {
        return edges.getLast();
    }

    @Override
    public int getBackDistance() {
        return backDistance;
    }

    @Nonnull
    @Override
    public Point2D getBackCoordinates() {
        return getEndCoordinates(backNode, unreachedBackNode, getBackEdge(), backDistance);
    }

    @Nonnull
    @Override
    public Point2D getBackPosition(Function<Node, Point2D> adapter) {
        Point2D back = adapter.apply(backNode);
        Point2D unreached = adapter.apply(unreachedBackNode);
        Point2D vector = getVector(back, unreached);
        double length =
            ((double) getBackDistance()) / getBackEdge().getLength() * vector.magnitude();
        return back.add(vector.normalize().multiply(length));
    }

    @Nonnull
    private Point2D getEndCoordinates(Node endNode, Node unreachedNode, Edge edge, int distance) {
        Point2D end = toPoint(endNode);
        Point2D unreached = toPoint(unreachedNode);
        Point2D vector = getVector(end, unreached);
        double endLength = ((double) distance) / edge.getLength() * vector.magnitude();
        vector = vector.normalize();
        return toPoint(endNode).add(vector.multiply(endLength));
    }

    @Nonnull
    private Point2D getVector(Point2D start, Point2D unreached) {
        return unreached.subtract(start);
    }

    @Nonnull
    private Point2D toPoint(Node node) {
        return node.getCoordinates().toPoint2D();
    }

    @Nonnull
    @Override
    public List<Point2D> getPositions(Function<Node, Point2D> adapter) {
        List<Point2D> points = new LinkedList<>();
        Point2D start = getFrontPosition(adapter);
        points.add(start);

        Iterator<Edge> iterator = edges.iterator();
        Edge first = iterator.next();

        if (!iterator.hasNext()) {
            points.add(getBackPosition(adapter));
            return points;
        }

        addNode(points, adapter.apply(frontNode));

        Edge last = first;
        while (iterator.hasNext()) {
            Edge edge = iterator.next();
            if (iterator.hasNext()) {
                Node uncommon = edge.getOtherNode(edge.getCommonNode(last));
                addNode(points, adapter.apply(uncommon));
            } else {
                addNode(points, getBackPosition(adapter));
            }
            last = edge;
        }

        return points;
    }

    private void addNode(List<Point2D> list, Point2D point) {
        if (list.isEmpty() || !list.get(list.size() - 1).equals(point)) {
            list.add(point);
        }
    }

    @Nonnull
    @Override
    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * Creates a position for the same train, moved by the specified distance.
     *
     * @param distance the distance in meters
     * @return a new TrainPosition
     */
    @Nonnull
    TrainPosition move(int distance) {
        int newDistance = getFrontDistance() + distance;
        return new TrainPosition(train,
            new LinkedList<>(edges),
            newDistance,
            frontNode,
            unreachedFrontNode,
            backDistance - distance,
            backNode,
            unreachedBackNode);
    }

    /**
     * Creates a position for the same train, moved by the specified distance.
     * This should only be called if the train's back is exactly at the start of the "newBack" Edge.
     *
     * @param newBack the Edge the train's back is at
     * @param movedDistance the distance in meters the train has moved
     * @return a new TrainPosition
     * @throws NullPointerException if newBack is null
     * @throws IllegalArgumentException if newBack can't be the new last edge
     * @see Train.EventFactory#leave(int, Edge, int)
     */
    @Nonnull
    TrainPosition leaveBack(Edge newBack, int movedDistance) {
        LinkedList<Edge> edges = new LinkedList<>(getEdges());
        edges.removeLast();
        Node unreachedBackNode = backNode;
        Node backNode = newBack.getOtherNode(unreachedBackNode);
        return new TrainPosition(getTrain(),
            edges,
            getFrontDistance() + movedDistance,
            frontNode,
            unreachedFrontNode,
            newBack.getLength(),
            backNode,
            unreachedBackNode);
    }

    /**
     * Creates a position for the same train at which the train's front just reached the "newStart"
     * Edge.
     *
     * @param newStart the Edge the train's front just reached
     * @return a new TrainPosition
     * @throws NullPointerException if newStart is null
     * @throws IllegalArgumentException if the train can't reach newStart from its current position
     * with a simple reach event
     * @see Train.EventFactory#reach(int, Edge, int)
     */
    @Nonnull
    TrainPosition reachFront(Edge newStart) {
        LinkedList<Edge> edges = new LinkedList<>(getEdges());
        edges.addFirst(newStart);
        Node frontNode = unreachedFrontNode;
        Node unreachedFrontNode = newStart.getOtherNode(frontNode);
        return new TrainPosition(getTrain(),
            edges,
            0,
            frontNode,
            unreachedFrontNode,
            calculateBackDistance(getTrain().getLength(), edges, 0),
            backNode,
            unreachedBackNode);
    }

    private static int calculateBackDistance(int trainLength, List<Edge> edges, int frontDistance) {
        if (edges.size() == 1) {
            return edges.get(0).getLength() - frontDistance + trainLength;
        }

        trainLength -= frontDistance;
        Iterator<Edge> iterator = edges.listIterator(1);
        while (iterator.hasNext()) {
            Edge edge = iterator.next();
            if (iterator.hasNext()) {
                // Subtract the lengths of all intermediate edges, just not the last one
                trainLength -= edge.getLength();
            }
        }

        return trainLength;
    }

    /**
     * Creates a position of the same train moved by the specified distance. If the front leaves the
     * current front edge while doing so, the possibleNewStart is assumed to be the new front edge.
     *
     * @param moveDistance the moved distance in meters
     * @param possibleNewStart the possible new front edge
     * @return a new TrainPosition
     * @throws NullPointerException if possibleNewStart is needed and null
     */
    @Nonnull
    TrainPosition interpolationMove(int moveDistance, @Nullable Edge possibleNewStart) {
        LinkedList<Edge> edges = new LinkedList<>(getEdges());
        Node frontNode = this.frontNode;
        Node unreachedFrontNode = this.unreachedFrontNode;
        int newDistance = getFrontDistance() + moveDistance;
        if (newDistance > getFrontEdge().getLength()) {
            edges.addFirst(possibleNewStart);
            newDistance -= getFrontEdge().getLength();
            frontNode = unreachedFrontNode;
            unreachedFrontNode = Objects.requireNonNull(possibleNewStart).getOtherNode(frontNode);
        }

        int trainLength = getTrain().getLength();
        Node backNode = this.backNode;
        Node unreachedBackNode = this.unreachedBackNode;
        int backDistance = calculateBackDistance(trainLength, edges, newDistance);
        while (backDistance < 0) {
            Edge removed = edges.removeLast();
            Edge newLast = edges.getLast();
            unreachedBackNode = backNode;
            backNode = newLast.getOtherNode(unreachedBackNode);
            backDistance += removed.getLength();
        }

        return new TrainPosition(getTrain(),
            edges,
            newDistance,
            frontNode,
            unreachedFrontNode,
            backDistance,
            backNode,
            unreachedBackNode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TrainPosition that = (TrainPosition) o;

        if (frontDistance != that.frontDistance) {
            return false;
        }
        if (!train.equals(that.train)) {
            return false;
        }
        if (!frontNode.equals(that.frontNode)) {
            return false;
        }
        return unreachedFrontNode.equals(that.unreachedFrontNode);
    }

    @Override
    public int hashCode() {
        int result = train.hashCode();
        result = 31 * result + frontNode.hashCode();
        result = 31 * result + unreachedFrontNode.hashCode();
        result = 31 * result + frontDistance;
        return result;
    }

    @Override
    public String toString() {
        return "TrainPosition{"
            + "train=" + train.getName()
            + ", frontEdge=" + getFrontEdge().getName()
            + ", frontNode=" + frontNode.getName()
            + ", unreachedFrontNode=" + unreachedFrontNode.getName()
            + ", frontDistance=" + frontDistance
            + ", backEdge=" + getBackEdge().getName()
            + ", backNode=" + backNode.getName()
            + ", unreachedBackNode=" + unreachedBackNode.getName()
            + ", backDistance=" + backDistance
            + '}';
    }
}
