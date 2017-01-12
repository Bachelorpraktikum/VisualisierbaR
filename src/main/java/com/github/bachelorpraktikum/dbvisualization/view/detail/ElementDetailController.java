package com.github.bachelorpraktikum.dbvisualization.view;

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

    void setElement(Element e, int time) {
        elementName.textProperty().setValue(e.getType().getName());

        try {
            String path = e.getType().getImageUrls().get(0).toExternalForm().replace("fxml", "png");
            Image image = new Image(path);
            System.out.println(path);
            elementImage.setImage(image);
        } catch (IndexOutOfBoundsException ignored) {

        }
        coordinateValue.textProperty().setValue(e.getNode().getCoordinates().toString());
    }

    void setTrain(Train t, int time) {
        elementName.textProperty().setValue(t.getReadableName());
        URL path = t.getClass().getResource(String.format("symbols/%s.png", "train"));
        Image image = new Image(path.toExternalForm());
        elementImage.setImage(image);

        t.getState(0);
    }
}
