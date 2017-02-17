package com.github.bachelorpraktikum.dbvisualization.model.train;

import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import javafx.geometry.Point2D;
import javax.annotation.Nonnull;
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

    static TrainPosition init(Train train, Edge edge, Node start, Node end) {
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
        return toPoint(node.getCoordinates());
    }

    @Nonnull
    private Point2D toPoint(Coordinates coordinates) {
        return new Point2D(coordinates.getX(), coordinates.getY());
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

    @Nonnull
    TrainPosition interpolationMove(int moveDistance, Edge possibleNewStart) {
        LinkedList<Edge> edges = new LinkedList<>(getEdges());
        Node frontNode = this.frontNode;
        Node unreachedFrontNode = this.unreachedFrontNode;
        int newDistance = getFrontDistance() + moveDistance;
        if (newDistance > getFrontEdge().getLength()) {
            edges.addFirst(possibleNewStart);
            newDistance -= getFrontEdge().getLength();
            frontNode = unreachedFrontNode;
            unreachedFrontNode = possibleNewStart.getOtherNode(frontNode);
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Train.Position)) {
            return false;
        }

        Train.Position that = (Train.Position) obj;

        if (!train.equals(that.getTrain())) {
            return false;
        }
        if (frontDistance != that.getFrontDistance()) {
            return false;
        }
        if (!getFrontEdge().equals(that.getFrontEdge())) {
            return false;
        }
        if (backDistance != that.getBackDistance()) {
            return false;
        }
        return getBackEdge().equals(that.getBackEdge());
    }

    @Override
    public int hashCode() {
        int result = getFrontEdge().hashCode();
        result = 31 * result + frontDistance;
        return result;
    }

    @Override
    public String toString() {
        return "Position{"
            + "edge=" + getFrontEdge()
            + ", frontDistance=" + frontDistance
            + '}';
    }
}
