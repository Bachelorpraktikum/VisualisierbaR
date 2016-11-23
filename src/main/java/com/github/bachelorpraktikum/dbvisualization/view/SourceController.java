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


        // Set the activeController based on the selected tab
        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) ->
                        activeController = getTabController(newValue.getContent().getId())
        );

        // Enable the "Open" button if path is set
        resourceURLProperty().addListener((observable, oldValue, newValue) ->
                openSource.setDisable(newValue == null || newValue.toString().isEmpty())
        );

        openSource.setOnAction(event -> {
            openMainWindow();
        });
    }

    /**
     * Gets the controller which corresponds to the current tab
     * The controller is chosen by the root pane id of the tab
     *
     * @param id ID of the current root pane of the selected tab
     * @return Controller which corresponds to the current tab
     */
    private SourceChooser getTabController(String id) {
        for (SourceChooser controller : controllers) {
            if (Objects.equals(id, controller.getRootPaneId())) {
                return controller;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRootPaneId() {
        return tabPane.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSource.Type getResourceType() {
        return activeController.getResourceType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getResourceURL() {
        return resourceURLProperty().getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReadOnlyProperty<URL> resourceURLProperty() {
        return activeController.resourceURLProperty();
    }

    /**
     * The {@link #rootPane} will be displayed on the given stage
     *
     * @param stage Stage on which the scene will be displayed
     */
    public void setStage(Stage stage) {
        this.stage = stage;

        Scene scene = new Scene(rootPane);
        stage.setScene(scene);
    }

    /**
     * Sets the scene for the current stage to the main view
     */
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
