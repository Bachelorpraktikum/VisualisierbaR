package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.DataSource;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javafx.beans.property.ReadOnlyProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class SourceController implements SourceChooser {
    @FXML
    private BorderPane rootPane;
    @FXML
    private FileChooserController fileChooserTabController;
    @FXML
    private TabPane tabPane;

    private Stage stage;

    @FXML
    private Button openSource;

    private SourceChooser activeController;
    private List<SourceChooser> controllers;

    @FXML
    private void initialize() {
        activeController = fileChooserTabController;

        controllers = new LinkedList<>();
        controllers.add(fileChooserTabController);


        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) ->
                        activeController = getTabController(newValue.getContent().getId())
        );

        resourceURLProperty().addListener((observable, oldValue, newValue) ->
                openSource.setDisable(newValue == null || newValue.toString().isEmpty())
        );

        openSource.setOnAction(event -> {
            openMainWindow();
        });
    }

    private SourceChooser getTabController(String id) {
        for (SourceChooser controller : controllers) {
            if (Objects.equals(id, controller.getRootPaneId())) {
                return controller;
            }
        }

        return null;
    }

    @Override
    public String getRootPaneId() {
        return tabPane.getId();
    }

    @Override
    public DataSource.Type getResourceType() {
        return activeController.getResourceType();
    }

    @Override
    public URL getResourceURL() {
        return resourceURLProperty().getValue();
    }

    @Override
    public ReadOnlyProperty<URL> resourceURLProperty() {
        return activeController.resourceURLProperty();
    }

    public void setStage(Stage stage) {
        this.stage = stage;

        Scene scene = new Scene(rootPane);
        stage.setScene(scene);
    }

    private void openMainWindow() {
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("MainView.fxml"));
        Pane mainPane = null;
        try {
            mainPane = mainLoader.load();
        } catch (IOException e) {
            // This should never happen (see load function)
            return;
        }
        stage.setScene(new Scene(mainPane));
        // MainViewController controller = mainLoader.getController();
        // controller.setURL(getResourceURL());
    }
}
