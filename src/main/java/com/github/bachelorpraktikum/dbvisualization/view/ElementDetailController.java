package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.model.Element;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ElementDetailController {
    @FXML
    public VBox detailBox;
    @FXML
    public Label elementName;
    @FXML
    public ImageView elementImage;
    @FXML
    public Label coordinateValue;
    @FXML
    public Label custom;
    @FXML
    public Label customValue;
    @FXML
    public LineChart vt_chart;
    @FXML
    public LineChart vd_chart;
    @FXML
    public LineChart dt_chart;

    void setElement(Element e) {
        elementName.textProperty().setValue(e.getName());

        try {
            Image image = new Image(e.getType().getImageUrls().get(0).getPath());
            elementImage.setImage(image);
        } catch (IndexOutOfBoundsException ignored) {

        }
        coordinateValue.textProperty().setValue(e.getNode().getCoordinates().toString());
    }
}
