package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.model.GraphObject;
import com.github.bachelorpraktikum.dbvisualization.view.TooltipUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;

public class DetailsController {

    @FXML
    private Node root;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Pane content;
    @FXML
    private Label elementName;
    @FXML
    private Group elementImage;
    @FXML
    private Button closeButton;

    private ObjectProperty<DetailsBase<?>> details;

    public DetailsController() {
        details = new SimpleObjectProperty<>();
    }

    @FXML
    private void initialize() {
        details.addListener((observable, oldValue, newValue) -> {
            content.getChildren().clear();
            if (newValue != null) {
                content.getChildren().add(newValue.getDetails());
            }
        });

        TooltipUtil.install(elementName, () -> details.get().getName());
    }

    public void setDetail(DetailsBase<GraphObject<?>> detail) {
        details.set(detail);
        elementName.textProperty().setValue(detail.getName());

        elementImage.getChildren().clear();
        Shape shape = detail.getShape();
        resizeShape(shape, 30);
        elementImage.getChildren().add(shape);
    }

    private void resizeShape(Shape shape, double max) {
        Bounds shapeBounds = shape.getBoundsInParent();
        double maxShape = Math.max(shapeBounds.getWidth(), shapeBounds.getHeight());
        double factor = max / maxShape;
        shape.setScaleX(shape.getScaleX() * factor);
        shape.setScaleY(shape.getScaleY() * factor);
    }

    public void setOnClose(EventHandler<ActionEvent> onClose) {
        closeButton.setOnAction(onClose);
    }
}
