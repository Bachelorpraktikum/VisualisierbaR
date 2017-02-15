package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.DataSource;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import javafx.beans.property.ReadOnlyProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class SourceController implements SourceChooser {
    @FXML
    private Button closeWindowButton;
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

        fireOnEnterPress(openSource);
        fireOnEnterPress(closeWindowButton);

        // Set the activeController based on the selected tab
        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) ->
                        activeController = getTabController(newValue.getContent().getId())
        );

        // Enable the "Open" button if path is set
        resourceURIProperty().addListener((observable, oldValue, newValue) ->
                openSource.setDisable(newValue == null || newValue.toString().isEmpty())
        );

        openSource.setOnAction(event -> openMainWindow());

        closeWindowButton.setOnAction(event -> closeWindow());
    }

    /**
     * Adds an EventHandler to the button which fires the button on pressing enter
     *
     * @param button Button to add eventHandler to
     */
    private void fireOnEnterPress(Button button) {
        button.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                button.fire();
            }
        });
    }

    /**
     * Gets the controller which corresponds to the current tab.
     * The controller is chosen by the root pane id of the tab.
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
    @Nonnull
    @Override
    public String getRootPaneId() {
        return tabPane.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public DataSource.Type getResourceType() {
        return activeController.getResourceType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public URI getResourceURI() {
        return resourceURIProperty().getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ReadOnlyProperty<URI> resourceURIProperty() {
        return activeController.resourceURIProperty();
    }

    /**
     * The {@link #rootPane} will be displayed on the given stage.
     *
     * @param stage Stage on which the scene will be displayed
     */
    public void setStage(Stage stage) {
        this.stage = stage;

        Scene scene = new Scene(rootPane);
        stage.setScene(scene);

        stage.centerOnScreen();
    }

    /**
     * Sets the scene for the current stage to the main view.
     */
    private void openMainWindow() {
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("MainView.fxml"));
        mainLoader.setResources(ResourceBundle.getBundle("bundles.localization"));
        try {
            mainLoader.load();
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).severe("Main window couldn't be opened\n" + e);
            return;
        }
        MainController controller = mainLoader.getController();
        controller.setStage(stage);
        controller.setDataSource(new DataSource(DataSource.Type.LOG_FILE, getResourceURI()));
    }

    private void closeWindow() {
        Stage primaryStage = (Stage) rootPane.getScene().getWindow();
        primaryStage.close();
    }
}
