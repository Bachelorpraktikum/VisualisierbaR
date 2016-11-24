package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.DataSource;
import com.github.bachelorpraktikum.dbvisualization.logparser.GraphParser;
import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Event;
import com.github.bachelorpraktikum.dbvisualization.model.Train;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;

import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class MainController {
    @FXML
    private Button closeButton;
    @FXML
    private BorderPane rootPane;

    @FXML
    private Pane leftPane;
    @FXML
    private ToggleButton logToggle;
    @FXML
    private ListView<Event> logList;

    private Stage stage;

    @FXML
    private void initialize() {
        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showSourceChooser();
            }
        });

        // Hide logList by default
        rootPane.setLeft(null);
        logToggle.selectedProperty().addListener((observable, oldValue, newValue) ->
                rootPane.setLeft(newValue ? leftPane : null)
        );

        Callback<ListView<Event>, ListCell<Event>> listCellFactory = new Callback<ListView<Event>, ListCell<Event>>() {
            private final StringConverter<Event> stringConverter = new StringConverter<Event>() {
                @Override
                public String toString(Event event) {
                    return event.getDescription();
                }

                @Override
                public Event fromString(String string) {
                    return null;
                }
            };

            private final Callback<ListView<Event>, ListCell<Event>> factory = TextFieldListCell.forListView(stringConverter);

            @Override
            public ListCell<Event> call(ListView<Event> param) {
                ListCell<Event> result = factory.call(param);
                Tooltip tooltip = new Tooltip();

                result.setOnMouseEntered(event -> {
                    tooltip.setText(result.getText());
                    Bounds bounds = result.localToScreen(result.getBoundsInLocal());
                    tooltip.show(result, bounds.getMinX(), bounds.getMaxY());
                });
                result.setOnMouseExited(event -> tooltip.hide());

                return result;
            }
        };
        logList.setCellFactory(listCellFactory);
    }

    void setStage(@Nonnull Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(rootPane));
    }

    void setDataSource(@Nonnull DataSource source) {
        switch (source.getType()) {
            case LOG_FILE:
                Context context = null;
                try {
                    context = new GraphParser(source.getUrl().getFile()).parse();
                } catch (IOException | RuntimeException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Verarbeitungs-Fehler");
                    alert.setContentText("Can't parse chosen data source");
                    alert.show();
                    showSourceChooser();
                    return;
                }
                ContextHolder.getInstance().setContext(context);

                List<ObservableList<? extends Event>> lists = new LinkedList<>();
                lists.add(Element.in(context).getEvents());
                for (Train train : Train.in(context).getAll()) {
                    lists.add(train.getStates());
                }

                ObservableList<Event> events = new CompositeObservableEventList(lists);
                logList.setItems(new SortedList<>(events));
                return;
            default:
                return;
        }
    }

    private void showSourceChooser() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SourceChooser.fxml"));
        loader.setResources(ResourceBundle.getBundle("bundles.localization"));
        try {
            loader.load();
        } catch (IOException e) {
            // This should never happen, because the location is set (see load function)
            return;
        }
        SourceController controller = loader.getController();
        controller.setStage(stage);
    }
}
