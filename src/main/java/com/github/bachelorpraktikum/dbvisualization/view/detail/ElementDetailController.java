package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.model.Event;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train.State;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
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
    private LineChart<Double, Integer> vtChart;
    @FXML
    private LineChart<Double, Integer> vdChart;
    @FXML
    private LineChart<Double, Double> dtChart;
    @FXML
    private VBox trainBox;
    @FXML
    private Label speedValue;
    private ElementDetailBase detail;
    @FXML
    private Label lengthValue;

    private List<Object> bindings;

    @FXML
    private void initialize() {
        bindings = new LinkedList<>();
    }

    public void setDetail(ElementDetailBase detail) {
        bindings.clear();
        if (detail == null) {
            return;
        }

        this.detail = detail;

        trainBox.setVisible(detail.isTrain());
        elementBox.setVisible(!detail.isTrain());

        elementName.textProperty().setValue(detail.getName());

        Binding<String> coordBinding = Bindings.createStringBinding(() ->
                String.valueOf(detail.getCoordinatesString(detail.getCoordinates())),
            detail.timeProperty()
        );
        bindings.add(coordBinding);
        coordinateValue.textProperty().bind(coordBinding);

        Shape shape = detail.getShape();

        if (detail.isTrain()) {
            TrainDetail trainDetail = (TrainDetail) detail;

            Binding<String> backCoordBinding = Bindings.createStringBinding(() ->
                    detail.getCoordinatesString(trainDetail.getBackCoordinate()),
                detail.timeProperty()
            );
            bindings.add(backCoordBinding);
            coordinateValueBack.textProperty().bind(backCoordBinding);

            coordinateLabel.setText(
                ResourceBundle.getBundle("bundles.localization").getString("coordinate_front")
            );
            lengthValue.textProperty().setValue(String.format("%dm", trainDetail.getLength()));

            Binding<String> speedBinding = Bindings.createStringBinding(() ->
                    String.format("%dm/s", trainDetail.getSpeed()),
                trainDetail.timeProperty()
            );
            bindings.add(speedBinding);
            speedValue.textProperty().bind(speedBinding);

            ChangeListener<Number> chartListener = ((observable, oldValue, newValue) ->
                updateCharts(newValue.intValue())
            );
            detail.timeProperty().addListener(chartListener);
        } else {
            Binding<String> stateBinding = Bindings.createStringBinding(() ->
                    String.valueOf(((ElementDetail) detail).getState()),
                detail.timeProperty()
            );
            bindings.add(stateBinding);
            stateValue.textProperty().bind(stateBinding);
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

        Function<State, Double> distanceFunction = s -> s.getTotalDistance() / 1000.0;
        Function<State, Double> timeFunction = s -> s.getTime() / 1000.0;

        updateChart(vtChart, timeFunction, State::getSpeed, time);
        updateChart(vdChart, distanceFunction, State::getSpeed, time);
        updateChart(dtChart, timeFunction, distanceFunction, time);
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
            if (event.getTime() < 0) {
                continue;
            }
            state = train.getState(event.getTime(), state);
            data.add(new Data<>(xFunction.apply(state), yFunction.apply(state)));
        }
        state = train.getState(time, state);
        data.add(new Data<>(xFunction.apply(state), yFunction.apply(state)));
        chart.setData(FXCollections.singletonObservableList(new Series<>(data)));
    }
}
