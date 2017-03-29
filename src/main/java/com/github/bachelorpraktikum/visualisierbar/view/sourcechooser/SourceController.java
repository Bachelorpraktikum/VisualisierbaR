package com.github.bachelorpraktikum.visualisierbar.view.sourcechooser;

import com.github.bachelorpraktikum.visualisierbar.Visualisierbar;
import com.github.bachelorpraktikum.visualisierbar.datasource.DataSource;
import com.github.bachelorpraktikum.visualisierbar.model.Context;
import com.github.bachelorpraktikum.visualisierbar.view.MainController;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class SourceController {

    @FXML
    private Button closeWindowButton;
    @FXML
    private BorderPane rootPane;
    @FXML
    private Node fileChooserTab;
    @FXML
    private FileChooserController fileChooserTabController;
    @FXML
    private Node databaseChooserTab;
    @FXML
    private DatabaseChooserController databaseChooserTabController;
    @FXML
    private Node restChooserTab;
    @FXML
    private RestChooserController restChooserTabController;
    @FXML
    private TabPane tabPane;

    private Stage stage;

    @FXML
    private Button openSource;

    private Property<SourceChooser<?>> activeController;
    private Map<Node, SourceChooser<?>> controllers;

    // needs to be stored as a field, otherwise it will be garbage collected
    private BooleanBinding openDisabled;

    @FXML
    private void initialize() {
        this.activeController = new SimpleObjectProperty<>(fileChooserTabController);

        controllers = new HashMap<>();
        controllers.put(fileChooserTab, fileChooserTabController);
        controllers.put(databaseChooserTab, databaseChooserTabController);
        controllers.put(restChooserTab, restChooserTabController);

        fireOnEnterPress(openSource);
        fireOnEnterPress(closeWindowButton);

        // Set the activeController based on the selected tab
        tabPane.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                SourceChooser<?> controller = getTabController(newValue.getContent());
                activeController.setValue(controller);
                if (controller == null) {
                    openDisabled = null;
                } else {
                    openDisabled = Bindings.createBooleanBinding(
                        () -> !controller.inputChosen().get(),
                        controller.inputChosen()
                    );
                    openSource.disableProperty().bind(openDisabled);
                }
            }
        );

        openDisabled = Bindings.createBooleanBinding(
            () -> !activeController.getValue().inputChosen().get(),
            activeController.getValue().inputChosen()
        );
        openSource.disableProperty().bind(openDisabled);
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
     * @param node the content of the tab
     * @return Controller which corresponds to the current tab
     */
    private SourceChooser getTabController(Node node) {
        return controllers.get(node);
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
        DataSource dataSource = null;
        try {
            dataSource = activeController.getValue().getResource();
            Context context = dataSource.getContext();
            if (com.github.bachelorpraktikum.visualisierbar.model.Node.in(context)
                .getAll().isEmpty()) {
                throw new IOException("No valid input");
            }
        } catch (IOException e) {
            e.printStackTrace();
            ResourceBundle bundle = ResourceBundle.getBundle("bundles.localization");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            String headerText = bundle.getString("parse_error_header");
            alert.setHeaderText(headerText);
            String contentText = bundle.getString("parse_error_content");
            contentText = String.format(contentText, e.getMessage());
            alert.setContentText(contentText);
            alert.showAndWait();

            if (dataSource != null) {
                try {
                    dataSource.close();
                } catch (IOException e1) {
                    Logger.getLogger(getClass().getName())
                        .info(String.format("Error closing datasource: %s", e1));
                }
            }
            return;
        }

        FXMLLoader mainLoader = new FXMLLoader(MainController.class.getResource("MainView.fxml"));
        mainLoader.setResources(ResourceBundle.getBundle("bundles.localization"));
        try {
            mainLoader.load();
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).severe("Main window couldn't be opened\n" + e);
            return;
        }

        MainController controller = mainLoader.getController();
        controller.setStage(stage);
        controller.setDataSource(dataSource);
    }

    private void closeWindow() {
        Stage primaryStage = (Stage) rootPane.getScene().getWindow();
        primaryStage.close();
    }

    @FXML
    private void showLicenceInfo() {
        Visualisierbar.licenceInfo.showAndWait();
    }
}
