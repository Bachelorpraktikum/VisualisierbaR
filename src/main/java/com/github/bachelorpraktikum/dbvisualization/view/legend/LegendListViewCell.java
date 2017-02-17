package com.github.bachelorpraktikum.dbvisualization.view.legend;

import java.io.IOException;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class LegendListViewCell extends ListCell<LegendItem> {

    @FXML
    private Label eleName;
    @FXML
    private Group eleImage;
    @FXML
    private CheckBox checkbox;
    @FXML
    private AnchorPane cell;

    private Binding<LegendItem.State> binding;

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
            Shape shape = element.getImage();

            resizeShape(shape, 20);

            eleName.setText(name);
            eleImage.getChildren().clear();
            eleImage.getChildren().add(shape);
            // load with fxml

            setGraphic(cell);

            binding = Bindings.createObjectBinding(() -> {
                if (checkbox.isIndeterminate()) {
                    return LegendItem.State.AUTO;
                } else if (checkbox.isSelected()) {
                    return LegendItem.State.ENABLED;
                } else {
                    return LegendItem.State.DISABLED;
                }
            }, checkbox.selectedProperty(), checkbox.indeterminateProperty());
            element.stateProperty().bind(binding);
        }
    }

    private void resizeShape(Shape shape, double max) {
        Bounds shapeBounds = shape.getBoundsInParent();
        double maxShape = Math.max(shapeBounds.getWidth(), shapeBounds.getHeight());
        double factor = max / maxShape;
        shape.setScaleX(shape.getScaleX() * factor);
        shape.setScaleY(shape.getScaleY() * factor);
    }
}
