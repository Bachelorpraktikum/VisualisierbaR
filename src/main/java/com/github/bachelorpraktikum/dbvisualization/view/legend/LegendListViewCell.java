package com.github.bachelorpraktikum.dbvisualization.view.legend;

import com.github.bachelorpraktikum.dbvisualization.model.Shapeable;
import com.github.bachelorpraktikum.dbvisualization.view.ContextHolder;
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

public class LegendListViewCell extends ListCell<Shapeable> {

    @FXML
    private Label eleName;
    @FXML
    private Group eleImage;
    @FXML
    private CheckBox checkbox;
    @FXML
    private AnchorPane cell;


    protected void updateItem(Shapeable element, boolean empty) {
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
            Shape shape = element.createIconShape();

            if(shape == null) {
                System.out.println(element.getClass().getName());
            }
            resizeShape(shape, 20);

            eleName.setText(name);
            eleImage.getChildren().clear();
            eleImage.getChildren().add(shape);
            // load with fxml

            setGraphic(cell);

            Binding<Shapeable.State> binding = Bindings.createObjectBinding(() -> {
                if (checkbox.isIndeterminate()) {
                    return Shapeable.State.AUTO;
                } else if (checkbox.isSelected()) {
                    return Shapeable.State.ENABLED;
                } else {
                    return Shapeable.State.DISABLED;
                }
            }, checkbox.selectedProperty(), checkbox.indeterminateProperty());
            ContextHolder.getInstance().getContext().addObject(binding);
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
