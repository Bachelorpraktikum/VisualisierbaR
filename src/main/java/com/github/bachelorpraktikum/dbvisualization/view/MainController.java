package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.DataSource;
import com.github.bachelorpraktikum.dbvisualization.logparser.GraphParser;
import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Event;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import com.github.bachelorpraktikum.dbvisualization.view.graph.Graph;
import com.github.bachelorpraktikum.dbvisualization.view.graph.GraphShape;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.SimpleCoordinatesAdapter;
import com.github.bachelorpraktikum.dbvisualization.view.legend.LegendItem;
import com.github.bachelorpraktikum.dbvisualization.view.legend.LegendListViewCell;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class MainController {
    @FXML
    private ListView<String> elementList;
    @FXML
    private CheckBox elementFilter;
    @FXML
    private CheckBox trainFilter;
    @FXML
    private TextField filterText;

    @FXML
    private StackPane sidebar;
    @FXML
    private ListView<LegendItem> legend;
    @FXML
    private ToggleButton legendButton;
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

    @FXML
    private Pane centerPane;
    @Nullable
    private Graph graph;

    private Stage stage;

    @FXML
    private void initialize() {
        fireOnEnterPress(closeButton);
        fireOnEnterPress(logToggle);
        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showSourceChooser();
            }
        });

        legendButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            legend.setVisible(newValue);
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
        logList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                Element.in(ContextHolder.getInstance().getContext()).setTime(newValue.getTime())
        );

        ChangeListener<Number> boundsListener = (observable, oldValue, newValue) -> {
            if (ContextHolder.getInstance().hasContext()) {
                fitGraphToCenter(getGraph());
            }
        };
        centerPane.heightProperty().addListener(boundsListener);
        centerPane.widthProperty().addListener(boundsListener);
    }

    /**
     * Adds an EventHandler to the button which fires the button on pressing enter
     *
     * @param button Button to add eventHandler to
     */
    private void fireOnEnterPress(ButtonBase button) {
        button.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                button.fire();
            }
        });
    }

    void setStage(@Nonnull Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(rootPane));

        stage.centerOnScreen();
    }

    private void showLegend() {
        Context context = ContextHolder.getInstance().getContext();
        Stream<LegendItem> str = Element.in(context).getAll().stream()
                .map(Element::getType)
                .distinct()
                .map(type -> new LegendItem(LegendItem.Type.ELEMENT, type.getName()));

        legend.setItems(FXCollections.observableArrayList(str.collect(Collectors.toList())));
        Train t = Train.in(context).getAll().stream().iterator().next();
        legend.getItems().add(new LegendItem(LegendItem.Type.TRAIN, t.getName()));
        legend.setCellFactory(studentListView -> new LegendListViewCell());
    }

    private void showElements() {
        Context context = ContextHolder.getInstance().getContext();

        Stream<String> elements = Element.in(context).getAll().stream()
                .map(Element::getName).filter(el -> elementFilter.isSelected());
        Stream<String> trains = Train.in(context).getAll().stream()
                .map(Train::getName).filter(el -> trainFilter.isSelected());

        ObservableList<String> items = FXCollections.observableList(
                Stream.concat(elements, trains)
                        .filter(t -> t.toLowerCase().contains(filterText.getText().trim().toLowerCase()))
                        .collect(Collectors.toList())
        );

        elementList.setItems(items);
        // legend.setCellFactory();
    }

    void setDataSource(@Nonnull DataSource source) {
        switch (source.getType()) {
            case LOG_FILE:
                Context context = null;
                try {
                    context = new GraphParser(source.getUrl().getFile()).parse();
                } catch (IOException | RuntimeException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    String headerText = ResourceBundle.getBundle("bundles.localization").getString("parse_error_header");
                    alert.setHeaderText(headerText);
                    String contentText = ResourceBundle.getBundle("bundles.localization").getString("parse_error_content");
                    alert.setContentText(contentText);
                    alert.show();
                    showSourceChooser();
                    return;
                }
                ContextHolder.getInstance().setContext(context);

                List<ObservableList<? extends Event>> lists = new LinkedList<>();
                lists.add(Element.in(context).getEvents());
                for (Train train : Train.in(context).getAll()) {
                    lists.add(train.getEvents());
                }

                ObservableList<Event> events = new CompositeObservableList<>(lists);
                logList.setItems(events.sorted());
                fitGraphToCenter(getGraph());

                break;
            default:
                return;
        }

        showLegend();
        showElements();
        trainFilter.selectedProperty().addListener((observable, oldValue, newValue) -> showElements());
        elementFilter.selectedProperty().addListener((observable, oldValue, newValue) -> showElements());
        filterText.textProperty().addListener((observable, oldValue, newValue) -> showElements());
    }

    /**
     * Gets the current graph shape.<br>
     * If no graph shape exists, this method creates one and returns it.
     *
     * @return the graph shape
     * @throws IllegalStateException if there is no context
     */
    @Nonnull
    private Graph getGraph() {
        if (graph == null) {
            Context context = ContextHolder.getInstance().getContext();
            graph = new Graph(context, new SimpleCoordinatesAdapter());
            addToCenter(graph.getNodes().values());
            addToCenter(graph.getEdges().values());
            addToCenter(graph.getElements().values());
        }
        return graph;
    }

    private void addToCenter(Collection<GraphShape> values) {
        ObservableList<Node> children = centerPane.getChildren();
        for (GraphShape graphShape : values) {
            Shape shape = graphShape.getValue();
            children.add(shape);
            graphShape.addListener((observable, oldValue, newValue) -> {
                children.remove(oldValue);
                children.add(newValue);
            });
        }
    }

    private void fitGraphToCenter(Graph graph) {
        double minCenter = Math.min(centerPane.getHeight(), centerPane.getWidth()) - 10;

        Bounds graphBounds = graph.getBounds();
        double maxGraph = Math.max(graphBounds.getHeight(), graphBounds.getWidth());

        double scaleFactor = minCenter / maxGraph;

        if (!Double.isFinite(scaleFactor)) {
            scaleFactor = 1;
        }

        if (scaleFactor <= 0) {
            scaleFactor = 1;
        }

        graph.scale(scaleFactor);
        moveGraphToCenter(graph);
    }

    private void moveGraphToCenter(Graph graph) {
        Bounds graphBounds = graph.getBounds();

        double finalX = (centerPane.getWidth() - graphBounds.getWidth()) / 2;
        double xTranslate = finalX - graphBounds.getMinX();

        double finalY = (centerPane.getHeight() - graphBounds.getHeight()) / 2;
        double yTranslate = finalY - graphBounds.getMinY();

        graph.move(xTranslate, yTranslate);
    }

    private void showSourceChooser() {
        graph = null;
        ContextHolder.getInstance().setContext(null);
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
