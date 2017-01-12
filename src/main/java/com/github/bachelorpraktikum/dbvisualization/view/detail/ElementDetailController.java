package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.model.train.Train;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ElementDetailController {
    @FXML
    private VBox elementBox;
    @FXML
    private Label stateValue;
    @FXML
    private VBox detailBox;
    @FXML
    private Label elementName;
    @FXML
    private ImageView elementImage;
    @FXML
    private Label coordinateValue;
    @FXML
    private LineChart vt_chart;
    @FXML
    private LineChart vd_chart;
    @FXML
    private LineChart dt_chart;
    @FXML
    private VBox trainBox;
    @FXML
    private Label speedValue;
    private Train train;
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
        // elementImage.setImage(new Image(detail.getImageURL().toExternalForm()));
        coordinateValue.textProperty().setValue(String.valueOf(detail.getCoordinatesString()));

        if (detail.isTrain()) {
            TrainDetail trainDetail = (TrainDetail) detail;
            speedValue.textProperty().setValue(String.format("%dkm/h", trainDetail.getSpeed()));
            lengthValue.textProperty().setValue(String.format("%dm", trainDetail.getLength()));
        } else {
            stateValue.textProperty().setValue(String.valueOf(((ElementDetail) detail).getState()));
        }
    }

    public void setTime(int time) {
        if (detail != null) {
            detail.setTime(time);
            setDetail(detail);
        }
    }
}
