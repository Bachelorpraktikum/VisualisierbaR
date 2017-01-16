package com.github.bachelorpraktikum.dbvisualization.view.train;

import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import com.github.bachelorpraktikum.dbvisualization.view.TooltipUtil;
import com.github.bachelorpraktikum.dbvisualization.view.graph.Graph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.StrokeLineCap;

public final class TrainView {
    private static final double TRAIN_WIDTH = 0.3;

    private final Train train;
    private final IntegerProperty timeProperty;
    private final Function<Node, Point2D> coordinatesTranslator;
    private final double calibrationBase;
    private final Path path;

    public TrainView(Train train, Graph graph) {
        this.train = train;
        this.coordinatesTranslator = graph.getCoordinatesAdapter();
        this.calibrationBase = graph.getCoordinatesAdapter().getCalibrationBase();
        this.timeProperty = new SimpleIntegerProperty(0);

        this.path = new Path();
        path.setStrokeWidth(TRAIN_WIDTH * calibrationBase);
        path.setStroke(Color.GREEN);
        path.setStrokeLineCap(StrokeLineCap.BUTT);
        path.setOpacity(0.5);
        graph.getGroup().getChildren().add(path);
        path.toBack();

        timeProperty.addListener(((observable, oldValue, newValue) -> updateTrain(newValue.intValue())));
        updateTrain(0);

        TooltipUtil.install(path, new Tooltip(train.getReadableName() + " " + train.getLength()));
    }

    public Train getTrain() {
        return train;
    }

    public IntegerProperty timeProperty() {
        return timeProperty;
    }

    private void updateTrain(int time) {
        path.getElements().clear();
        Train.State state = train.getState(time);
        if(!state.isInitialized()) {
            return;
        }
        Train.Position trainPosition = state.getPosition();
        List<Edge> edges = trainPosition.getEdges();
        List<PathElement> elements = new LinkedList<>();

        if (edges.size() == 1) {
            // just let the train go from node 1 to node 2
            Edge edge = edges.get(0);
            Point2D pos1 = toPos(edge.getNode1());
            Point2D vector = toPos(edge.getNode2()).subtract(pos1);
            double edgeLength = vector.magnitude();
            vector = vector.normalize();

            double backDistance = ((double) trainPosition.getBackDistance()) / edge.getLength() * edgeLength;
            Point2D back = pos1.add(vector.multiply(backDistance));
            elements.add(new MoveTo(back.getX(), back.getY()));

            double frontDistance = ((double) trainPosition.getFrontDistance()) / edge.getLength() * edgeLength;
            Point2D front = pos1.add(vector.multiply(frontDistance));
            elements.add(new LineTo(front.getX(), front.getY()));
        } else {
            // Look at the next edge to determine where the front is coming from
            Iterator<Edge> iterator = edges.iterator();
            Edge first = iterator.next();
            Edge second = iterator.next();
            Point2D start = toPos(findCommonNode(first, second));
            Point2D end = toPos(findNextNode(second, first));
            Point2D vector = end.subtract(start);
            double edgeLength = vector.magnitude();
            vector = vector.normalize();

            double frontDistance = ((double) trainPosition.getFrontDistance()) / first.getLength() * edgeLength;
            Point2D front = start.add(vector.multiply(frontDistance));
            elements.add(new MoveTo(front.getX(), front.getY()));
            if (!front.equals(start)) {
                elements.add(new LineTo(start.getX(), start.getY()));
            }

            Edge last = first;
            Edge current = second;
            while (iterator.hasNext()) {
                // if there is a next edge, this edge can be fully covered
                Edge nextEdge = iterator.next();
                Point2D nextPos = toPos(findCommonNode(current, nextEdge));
                elements.add(new LineTo(nextPos.getX(), nextPos.getY()));
                last = current;
                current = nextEdge;
            }

            // the last edge should not be fully covered
            start = toPos(findCommonNode(last, current));
            end = toPos(findNextNode(last, current));
            vector = end.subtract(start);
            edgeLength = vector.magnitude();
            vector = vector.normalize();
            double backDistance = ((double) trainPosition.getBackDistance()) / current.getLength() * edgeLength;
            Point2D back = start.add(vector.multiply(backDistance));
            elements.add(new LineTo(back.getX(), back.getY()));
        }

        path.getElements().addAll(elements);
    }

    private Point2D toPos(Node node) {
        return coordinatesTranslator.apply(node);
    }


    private Node findNextNode(Edge last, Edge current) {
        Node node1 = current.getNode1();
        Node node2 = current.getNode2();

        if (node1.equals(last.getNode1())
                || node1.equals(last.getNode2())) {
            return node2;
        } else if (node2.equals(last.getNode1())
                || node2.equals(last.getNode2())) {
            return node1;
        } else {
            throw new IllegalArgumentException("no common node");
        }
    }

    private Node findCommonNode(Edge edge1, Edge edge2) {
        Node node1 = edge2.getNode1();
        Node node2 = edge2.getNode2();

        if (node1.equals(edge1.getNode1())
                || node1.equals(edge1.getNode2())) {
            return node1;
        } else if (node2.equals(edge1.getNode1())
                || node2.equals(edge1.getNode2())) {
            return node2;
        } else {
            throw new IllegalArgumentException("no common node");
        }
    }
}
