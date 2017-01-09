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

public final class TrainView {
    private final Train train;
    private final IntegerProperty timeProperty;
    private final Function<Coordinates, Point2D> coordinatesTranslator;
    private final Path path;

    public TrainView(Train train, Graph graph) {
        this.train = train;
        this.coordinatesTranslator = graph::translate;
        this.timeProperty = new SimpleIntegerProperty(0);

        this.path = new Path();
        path.setStrokeWidth(0.3);
        path.setStroke(Color.GREEN);
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
        Train.Position trainPosition = train.getState(time).getPosition();

        List<PathElement> elements = new LinkedList<>();
        Iterator<Edge> iterator = trainPosition.getEdges().iterator();
        Edge last = iterator.next();
        Point2D lastPosition;
        if (iterator.hasNext()) {
            Edge next = iterator.next();
            Point2D frontStart = toPos(findNextNode(next, last));
            Point2D commonPosition = toPos(findCommonNode(next, last));

            Point2D frontVector = commonPosition.subtract(frontStart);
            double frontEdgeLength = frontVector.magnitude();
            double distance = ((next.getLength() - trainPosition.getFrontDistance()) / next.getLength()) * frontEdgeLength;
            Point2D startPosition = frontStart
                    .add(frontVector
                            .normalize()
                            .multiply(distance));

            elements.add(new MoveTo(startPosition.getX(), startPosition.getY()));
            elements.add(new LineTo(commonPosition.getX(), commonPosition.getY()));

            Point2D nextPosition = toPos(findNextNode(last, next));
            elements.add(new LineTo(nextPosition.getX(), nextPosition.getY()));

            last = next;
            lastPosition = nextPosition;
        } else {
            Point2D position = toPos(last.getNode1());
            elements.add(new MoveTo(position.getX(), position.getY()));

            position = toPos(last.getNode2());
            elements.add(new LineTo(position.getX(), position.getY()));
            lastPosition = position;
        }

        while (iterator.hasNext()) {
            Edge current = iterator.next();
            Node nextNode = findNextNode(last, current);
            Point2D position = toPos(nextNode);

            if (!iterator.hasNext()) {
                Point2D backVector = position.subtract(lastPosition);
                double backEdgeLength = backVector.magnitude();
                double distance = (trainPosition.getBackDistance() / current.getLength()) * backEdgeLength;
                position = lastPosition.add(backVector.normalize().multiply(distance));
            }

            elements.add(new LineTo(position.getX(), position.getY()));
            last = current;
            lastPosition = position;
        }

        path.getElements().addAll(elements);
    }

    private Point2D toPos(Node node) {
        return toPos(node.getCoordinates());
    }

    private Point2D toPos(Coordinates coordinates) {
        return coordinatesTranslator.apply(coordinates);
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

    private void addEdge(Path path, Edge edge) {
        Point2D start = coordinatesTranslator.apply(edge.getNode1().getCoordinates());
        Point2D end = coordinatesTranslator.apply(edge.getNode2().getCoordinates());
        path.getElements().addAll(
                new MoveTo(start.getX(), start.getY()),
                new LineTo(end.getX(), end.getY())
        );
    }
}
