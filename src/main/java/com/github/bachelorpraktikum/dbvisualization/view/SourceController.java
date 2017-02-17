package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.DataSource;
import com.github.bachelorpraktikum.dbvisualization.config.ConfigFile;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;
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
import javax.annotation.Nonnull;

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
        resourceUriProperty().addListener((observable, oldValue, newValue) ->
            openSource.setDisable(newValue == null || newValue.toString().isEmpty())
        );

        openSource.setOnAction(event -> openMainWindow());

        closeWindowButton.setOnAction(event -> closeWindow());
    }

    /**
     * Adds an EventHandler to the button which fires the button on pressing enter.
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

    public void setInitialDirectories(Map<DataSource.Type, URI> typeUriMapping) {
        for (Map.Entry<DataSource.Type, URI> entry : typeUriMapping.entrySet()) {
            SourceChooser controller = getControllerByType(entry.getKey());
            if (controller != null) {
                controller.setInitialUri(entry.getValue());
            }
        }
    }

    private SourceChooser getControllerByType(DataSource.Type type) {
        for (SourceChooser controller : controllers) {
            if (controller.getResourceType() == type) {
                return controller;
            }
        }

        return null;
    }

    @Nonnull
    @Override
    public String getRootPaneId() {
        return tabPane.getId();
    }

    @Nonnull
    @Override
    public DataSource.Type getResourceType() {
        return activeController.getResourceType();
    }

    @Override
    public void setInitialUri(URI initialUri) {

    }

    @Override
    @Nonnull
    public URI getResourceUri() {
        return resourceUriProperty().getValue();
    }

    @Nonnull
    @Override
    public ReadOnlyProperty<URI> resourceUriProperty() {
        return activeController.resourceUriProperty();
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
        controller.setDataSource(new DataSource(getResourceType(), getResourceUri()));

        String initialDirKey = String.format(geInitialDirKey(), getResourceType().toString());
        String parentFolder = new File(getResourceUri()).getParent();
        ConfigFile.getInstance().put(initialDirKey, parentFolder);
    }

    private String geInitialDirKey() {
        String logFileKey = ResourceBundle.getBundle("config_keys")
            .getString("initialDirectoryKey");
        return String.format(logFileKey, getResourceType().toString());
    }

    private void closeWindow() {
        Stage primaryStage = (Stage) rootPane.getScene().getWindow();
        primaryStage.close();
    }
}
