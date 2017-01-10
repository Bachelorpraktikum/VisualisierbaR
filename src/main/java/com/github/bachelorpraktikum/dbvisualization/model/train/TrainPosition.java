package com.github.bachelorpraktikum.dbvisualization.model.train;

import com.github.bachelorpraktikum.dbvisualization.model.Edge;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

@Immutable
@ParametersAreNonnullByDefault
final class TrainPosition implements Train.Position {
    private final Train train;
    private final LinkedList<Edge> edges;
    private final int frontDistance;
    private final int backDistance;

    private TrainPosition(Train train, LinkedList<Edge> edges, int frontDistance) {
        this.train = train;
        this.edges = edges;
        this.frontDistance = frontDistance;

        int trainLength = train.getLength() - getFrontDistance();

        if (trainLength <= 0) {
            this.backDistance = Math.abs(trainLength);
            Edge edge = edges.getFirst();
            edges.clear();
            edges.add(edge);
            return;
        }

        int backDistance = 0;
        Iterator<Edge> iterator = edges.listIterator(1);
        Edge edge;
        while (iterator.hasNext()) {
            edge = iterator.next();
            if (trainLength == 0) {
                iterator.remove();
            } else if (trainLength < edge.getLength()) {
                backDistance = trainLength;
                trainLength = 0;
            } else {
                trainLength -= edge.getLength();
            }
        }

        this.backDistance = backDistance;
    }

    static TrainPosition init(Train train, Edge edge) {
        LinkedList<Edge> edges = new LinkedList<>();
        edges.add(edge);
        return new TrainPosition(train, edges, Math.min(train.getLength(), edge.getLength()));
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
    public Edge getBackEdge() {
        return edges.getLast();
    }

    @Override
    public int getBackDistance() {
        return backDistance;
    }

    @Nonnull
    @Override
    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    @Nonnull
    TrainPosition move(int distance) {
        int newDistance = getFrontDistance() + distance;
        return new TrainPosition(getTrain(), new LinkedList<>(edges), newDistance);
    }

    @Nonnull
    TrainPosition leaveBack(Edge newBack, int movedDistance) {
        LinkedList<Edge> edges = new LinkedList<>(getEdges());
        edges.add(newBack);
        return new TrainPosition(getTrain(), edges, getFrontDistance() + movedDistance);
    }

    @Nonnull
    TrainPosition reachFront(Edge newStart) {
        LinkedList<Edge> edges = new LinkedList<>(getEdges());
        edges.addFirst(newStart);
        return new TrainPosition(getTrain(), edges, 0);
    }

    @Nonnull
    TrainPosition interpolationMove(int moveDistance, Edge possibleNewStart) {
        LinkedList<Edge> edges = new LinkedList<>(getEdges());
        int newDistance = getFrontDistance() + moveDistance;
        if (newDistance > getFrontEdge().getLength()) {
            edges.addFirst(possibleNewStart);
            newDistance -= getFrontEdge().getLength();
        }

        return new TrainPosition(getTrain(), edges, newDistance);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Train.Position)) return false;

        Train.Position that = (Train.Position) obj;

        if (!train.equals(that.getTrain())) return false;
        if (frontDistance != that.getFrontDistance()) return false;
        if (!getFrontEdge().equals(that.getFrontEdge())) return false;
        if (backDistance != that.getBackDistance()) return false;
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
