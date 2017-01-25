package com.github.bachelorpraktikum.dbvisualization.view.train;

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
        List<Point2D> points = trainPosition.getPositions(coordinatesTranslator);
        List<PathElement> elements = new LinkedList<>();

        Iterator<Point2D> iterator = points.iterator();
        Point2D start = iterator.next();
        elements.add(new MoveTo(start.getX(), start.getY()));

        while (iterator.hasNext()) {
            Point2D point = iterator.next();
            elements.add(new LineTo(point.getX(), point.getY()));
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
