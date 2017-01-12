package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;

import java.net.URL;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ElementDetailController {
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
    @FXML
    private Label lengthValue;

    public void setElement(Element e) {
        trainBox.setVisible(false);
        elementName.textProperty().setValue(e.getType().getName());

        try {
            String path = e.getType().getImageUrls().get(0).toExternalForm().replace("fxml", "png");
            Image image = new Image(path);
            elementImage.setImage(image);
        } catch (IndexOutOfBoundsException ignored) {

        }
        coordinateValue.textProperty().setValue(e.getNode().getCoordinates().toString());
        train = null;
    }

    public void setTrain(Train train, int time) {
        // trainBox.setVisible(true);
        elementName.textProperty().setValue(train.getReadableName());
        URL path = Train.class.getResource(String.format("../symbols/%s.png", "train"));
        Image image = new Image(path.toExternalForm());
        elementImage.setImage(image);

        Train.State s = train.getState(time);
        coordinateValue.textProperty().setValue(String.valueOf(train.getState(time).getPosition().getFrontEdge().getNode1().getCoordinates()));
        lengthValue.textProperty().setValue(String.format("%dm", train.getLength()));
        speedValue.textProperty().setValue(train.getName());
        speedValue.textProperty().setValue(String.format("%dkm/h", s.getSpeed()));
        this.train = train;
    }

    public void setTime(int time) {
        if (train != null) {
            setTrain(train, time);
        }
    }
}
