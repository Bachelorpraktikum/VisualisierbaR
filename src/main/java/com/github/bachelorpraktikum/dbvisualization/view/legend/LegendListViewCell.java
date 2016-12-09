package com.github.bachelorpraktikum.dbvisualization.view.legend;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;

public class LegendListViewCell extends ListCell<LegendItem> {
    @FXML
    private Label eleName;
    @FXML
    private ImageView eleImage;
    @FXML
    private CheckBox checkbox;
    @FXML
    private AnchorPane cell;

    protected void updateItem(LegendItem element, boolean empty) {
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
                Node listCell = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String name = element.getName();
            URL imageURL = element.getImageUrl();
            Image img = new Image(imageURL.toExternalForm());

            int space = 20;
            eleImage.setFitHeight(space);
            eleImage.setFitWidth(space);

            eleName.setText(name);
            eleImage.setImage(img);
            // eleImage.setGraphic to element.getImageURL():
            // load with fxml

            setGraphic(cell);
        }
    }
}
