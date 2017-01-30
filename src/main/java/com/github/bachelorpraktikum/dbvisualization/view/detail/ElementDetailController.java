package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.model.Event;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train.State;
import java.util.ResourceBundle;
import java.util.function.Function;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Shape;

public class ElementDetailController {
    @FXML
    private Label coordinateLabel;
    @FXML
    private Label coordinateValueBack;
    @FXML
    private VBox elementBox;
    @FXML
    private Label stateValue;
    @FXML
    private Label elementName;
    @FXML
    private Group elementImage;
    @FXML
    private Label coordinateValue;
    @FXML
    private LineChart<Integer, Integer> vt_chart;
    @FXML
    private LineChart<Double, Integer> vd_chart;
    @FXML
    private LineChart<Integer, Double> dt_chart;
    @FXML
    private VBox trainBox;
    @FXML
    private Label speedValue;
    private ElementDetailBase detail;
    @FXML
    private Label lengthValue;

    public void setDetail(ElementDetailBase detail) {
        if (detail == null) {
            return;
        }

        this.detail = detail;

        trainBox.setVisible(detail.isTrain());
        elementBox.setVisible(!detail.isTrain());

        elementName.textProperty().setValue(detail.getName());
        coordinateValue.textProperty().setValue(String.valueOf(detail.getCoordinatesString(detail.getCoordinates())));

        Shape shape = detail.getShape();

        if (detail.isTrain()) {
            TrainDetail trainDetail = (TrainDetail) detail;
            coordinateValueBack.textProperty().setValue(detail.getCoordinatesString(trainDetail.getBackCoordinate()));
            coordinateLabel.setText(ResourceBundle.getBundle("bundles.localization").getString("coordinate_front"));
            speedValue.textProperty().setValue(String.format("%dm/s", trainDetail.getSpeed()));
            lengthValue.textProperty().setValue(String.format("%dm", trainDetail.getLength()));
            shape.setRotate(180);
        } else {
            stateValue.textProperty().setValue(String.valueOf(((ElementDetail) detail).getState()));
        }

        if (!elementImage.getChildren().contains(shape)) {
            if (elementImage.getChildren().size() > 0) {
                elementImage.getChildren().remove(0);
            }
        }

        if (shape != null) {
            resizeShape(shape, 20);
            elementImage.getChildren().add(0, shape);
        }
    }

    private void resizeShape(Shape shape, double max) {
        Bounds shapeBounds = shape.getBoundsInParent();
        double maxShape = Math.max(shapeBounds.getWidth(), shapeBounds.getHeight());
        double factor = max / maxShape;
        shape.setScaleX(shape.getScaleX() * factor);
        shape.setScaleY(shape.getScaleY() * factor);
        shape.setRotate(180);
    }

    private void updateCharts(int time) {
        if (!detail.isTrain()) {
            return;
        }

        updateChart(vt_chart, State::getTime, State::getSpeed, time);
        updateChart(vd_chart, s -> s.getTotalDistance() / 1000.0, State::getSpeed, time);
        updateChart(dt_chart, State::getTime, s -> s.getTotalDistance() / 1000.0, time);
    }

    private <X, Y> void updateChart(LineChart<X, Y> chart,
            Function<State, X> xFunction,
            Function<State, Y> yFunction,
            int time) {
        Train train = (Train) detail.getElement();

        ObservableList<Data<X, Y>> data = FXCollections.observableArrayList();
        State state = train.getState(0);
        data.add(new Data<>(xFunction.apply(state), yFunction.apply(state)));
        for (Event event : train.getEvents()) {
            if (event.getTime() > time) {
                break;
            }
            if(event.getTime() < 0) {
                continue;
            }
            state = train.getState(event.getTime(), state);
            data.add(new Data<>(xFunction.apply(state), yFunction.apply(state)));
        }
        state = train.getState(time, state);
        data.add(new Data<>(xFunction.apply(state), yFunction.apply(state)));
        chart.setData(FXCollections.singletonObservableList(new Series<>(data)));
    }



    public void setTime(int time) {
        if (detail != null) {
            detail.setTime(time);
            setDetail(detail);
            updateCharts(time);
        }
    }
}
