package com.github.bachelorpraktikum.dbvisualization.view;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;


public class FileChooserController implements SourceChooser {
    @FXML
    private TextField pathField;
    @FXML
    private GridPane rootPane;

    @FXML
    private Button explorerButton;
    private FileChooser fileChooser;

    private ReadOnlyObjectWrapper<URL> fileURLProperty;

    @FXML
    private void initialize() {
        fileURLProperty = new ReadOnlyObjectWrapper<>();
        fileChooser = new FileChooser();
        explorerButton.setOnAction(event -> updatePath(openFileChooser()));

        fileURLProperty.addListener((observable, oldValue, newValue) -> {
            pathField.setText(String.valueOf(newValue));
        });
    }

    private File openFileChooser() {
        return fileChooser.showOpenDialog(rootPane.getScene().getWindow());
    }

    private void updatePath(File file) {
        try {
            fileURLProperty.setValue(file.getAbsoluteFile().toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public URL getResourceURL() {
        return fileURLProperty.getValue();
    }

    @Override
    public ReadOnlyObjectProperty<URL> resourceURLProperty() {
        return fileURLProperty.getReadOnlyProperty();
    }

    @Override
    public String getRootPaneId() {
        return rootPane.getId();
    }
}
