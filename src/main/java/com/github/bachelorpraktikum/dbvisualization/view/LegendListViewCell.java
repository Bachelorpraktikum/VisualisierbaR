package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;

class LegendListViewCell<T> extends ListCell<T> {
    @FXML
    private Label eleName;
    @FXML
    private ImageView eleImage;
    @FXML
    private CheckBox checkbox;
    @FXML
    private AnchorPane cell;

    protected void updateItem(T element, boolean empty) {
        super.updateItem(element, empty);
        if (empty) {
            setText(null);
            Rectangle emptyRect = new Rectangle();
            emptyRect.setOpacity(1);
            setGraphic(emptyRect);
        } else {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("legendCell.fxml"));
            loader.setController(this);
            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String name = "";
            String imageURL = "";

            if (element instanceof Element.Type) {
                name = ((Element.Type) element).getName();
                imageURL = ((Element.Type) element).getImageUrl();
            } else if (element instanceof Train) {
                name = Train.class.getSimpleName();
                imageURL = Element.class.getResource(String.format("symbols/%s.png", name)).toExternalForm();
            }
            imageURL = Element.class.getResource(String.format("symbols/%s.png", "test")).toExternalForm();
            Image img = new Image(imageURL);

            eleImage.setFitHeight(img.getHeight() / 10);
            eleImage.setFitWidth(cell.getWidth() / 20);

            eleName.setText(name);
            eleImage.setImage(img);
            // eleImage.setGraphic to element.getImageURL():
            // load with fxml

            setGraphic(cell);
        }
    }
}
