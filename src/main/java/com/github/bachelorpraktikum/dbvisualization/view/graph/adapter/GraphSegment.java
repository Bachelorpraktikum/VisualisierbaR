package com.github.bachelorpraktikum.dbvisualization.view.graph.adapter;

import com.github.bachelorpraktikum.dbvisualization.model.Node;

import java.util.LinkedList;

import javafx.geometry.Point2D;

class GraphSegment {
    private LinkedList<Node> nodes = new LinkedList<>();
    private SegmentType segmentType;
    private static ProportionalCoordinatesAdapter adapter;
    private Point2D startPoint;
    private Point2D endPoint;

    GraphSegment(SegmentType type) {
        this.segmentType = type;
    }

    void addNode(Node node) {
        if(nodes.isEmpty()) {
            this.startPoint = adapter.apply(node);
        }
        this.nodes.add(node);
    }

    SegmentType getSegmentType() {
        return this.segmentType;
    }

    void endSegment() {
        this.endPoint = adapter.apply(this.nodes.getLast());
    }

    public Point2D getStartPoint() {
        return this.startPoint;
    }

    public Point2D getEndPoint() {
        return this.endPoint;
    }

    boolean isSameTypeAs(GraphSegment other) {
        return this.segmentType.equals(other.getSegmentType());
    }

    LinkedList<Node> getNodes() {
        return this.nodes;
    }

    double getSignificantCoordinate() {
        if(segmentType == SegmentType.HORIZONTAL)
            return this.startPoint.getY();
        else
            return this.startPoint.getX();
    }

    public int getSize() {
        return nodes.size();
    }

    Point2D getNormalVector() {
        if(segmentType == SegmentType.HORIZONTAL)
            return new Point2D(0, -1);
        else
            return new Point2D(1, 0);
    }

    public static void setAdapter(ProportionalCoordinatesAdapter coordinatesAdapter) {
        adapter = coordinatesAdapter;
    }

    boolean checkForCollision(GraphSegment other) {
        if(this.getSignificantCoordinate() != other.getSignificantCoordinate())
            return false;

        // check if the start or end point of the other
        // segment is on the line represented by
        // this GraphSegment
        Point2D otherStart = other.getStartPoint();
        Point2D otherEnd = other.getEndPoint();
        Point2D thisStart = this.startPoint;
        Point2D thisEnd = this.endPoint;

        return isPointOnLine(thisStart, thisEnd, otherEnd) || isPointOnLine(thisStart, thisEnd, otherStart)
                || isPointOnLine(otherStart, otherEnd, thisStart) || isPointOnLine(otherStart, otherEnd, thisEnd);
    }

    private boolean isPointOnLine(Point2D start, Point2D end, Point2D p) {
        double AB = start.distance(end);
        double AP = start.distance(p);
        double BP = end.distance(p);

        return AB == AP + BP;
    }

    @Override
    public String toString() {
        if(this.nodes.size() >= 1) {
            return "Type: " + segmentType + " Start: " + startPoint + " End: " + endPoint;
        } else {
            return "Empty Segment. Type: " + segmentType;
        }
    }
}
