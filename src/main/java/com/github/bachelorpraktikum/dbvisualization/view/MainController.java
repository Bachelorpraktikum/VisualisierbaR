package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.DataSource;
import com.github.bachelorpraktikum.dbvisualization.database.Database;
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
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Group;
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
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class MainController {
    private Map<Context, List<ObservableValue>> listeners;

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
    private TextField timeText;
    @FXML
    private HBox rightSpacer;

    @FXML
    private Pane centerPane;
    @Nullable
    private Graph graph;

    private Stage stage;

    private Group rootGroup;

    private double SCALE_DELTA = 1.1;

    private double mousePressedX = -1;
    private double mousePressedY = -1;
    private Map<GraphObject<?>, ObservableValue<LegendItem.State>> legendStates;

    @FXML
    private void initialize() {
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        this.listeners = new WeakHashMap<>();
        this.rootGroup = new Group();
        this.legendStates = new HashMap<>(256);
        fireOnEnterPress(closeButton);
        fireOnEnterPress(logToggle);
        closeButton.setOnAction(event -> showSourceChooser());

        legendButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            legend.setVisible(newValue);
            if (newValue) {
                legend.toFront();
            } else
                legend.toBack();
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
                TooltipUtil.install(result, result::getText);

                return result;
            }
        };
        logList.setCellFactory(listCellFactory);
        logList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Element.in(ContextHolder.getInstance().getContext()).setTime(newValue.getTime());
            timeText.setText(String.format("%dms", newValue.getTime()));
        });

        ChangeListener<Number> boundsListener = (observable, oldValue, newValue) -> {
            if (ContextHolder.getInstance().hasContext()) {
                fitGraphToCenter(getGraph());
            }
        };
        centerPane.heightProperty().addListener(boundsListener);
        centerPane.widthProperty().addListener(boundsListener);
        centerPane.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                Bounds bounds = rootGroup.localToScene(rootGroup.getBoundsInLocal());
                double oldScale = rootGroup.getScaleX();
                double scaleFactor = oldScale * ((event.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA);
                double translateX = event.getScreenX() - (bounds.getWidth() / 2 + bounds.getMinX());
                double translateY = event.getScreenY() - (bounds.getHeight() / 2 + bounds.getMinY());
                double f = (scaleFactor / oldScale) - 1;

                rootGroup.setScaleX(scaleFactor);
                rootGroup.setScaleY(scaleFactor);
                rootGroup.setTranslateX(rootGroup.getTranslateX() - f * translateX);
                rootGroup.setTranslateY(rootGroup.getTranslateY() - f * translateY);
            }
        });
        centerPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mousePressedX = -1;
                mousePressedY = -1;
            }
        });
        centerPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (!event.isPrimaryButtonDown())
                    return;

                if (mousePressedX == -1 && mousePressedY == -1) {
                    mousePressedX = event.getX();
                    mousePressedY = event.getY();
                }

                double xOffset = (event.getX() - mousePressedX);
                double yOffset = (event.getY() - mousePressedY);

                centerPane.setTranslateX(centerPane.getTranslateX() + xOffset);
                centerPane.setTranslateY(centerPane.getTranslateY() + yOffset);
                event.consume();
            }
        });
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
        ObservableList<LegendItem> items = FXCollections.observableList(
                Stream.concat(Stream.of(new LegendItem(GraphObject.trains())),
                        Element.in(context).getAll().stream()
                                .map(Element::getType)
                                .distinct()
                                .map(GraphObject::element)
                                .map(LegendItem::new)
                ).collect(Collectors.toList())
        );

        for (LegendItem item : items) {
            legendStates.put(item.getGraphObject(), item.stateProperty());
        }

        legend.setItems(items);
        legend.setCellFactory(studentListView -> new LegendListViewCell());
    }

    private void showElements() {
        Context context = ContextHolder.getInstance().getContext();

        FilteredList<String> trains = FXCollections.observableList(Train.in(context).getAll().stream()
                .map(Train::getReadableName).collect(Collectors.toList()))
                .filtered(null);
        ObservableValue<Predicate<String>> trainBinding = Bindings.createObjectBinding(() -> s ->
                        trainFilter.isSelected(),
                trainFilter.selectedProperty());
        listeners.get(context).add(trainBinding);
        trains.predicateProperty().bind(trainBinding);

        FilteredList<String> elements = FXCollections.observableList(Element.in(context).getAll().stream()
                .map(Element::getName).collect(Collectors.toList())
        ).filtered(null);
        ObservableValue<Predicate<String>> elementBinding = Bindings.createObjectBinding(() -> s ->
                        elementFilter.isSelected(),
                elementFilter.selectedProperty());
        listeners.get(context).add(elementBinding);
        elements.predicateProperty().bind(elementBinding);

        List<ObservableList<? extends String>> lists = Arrays.asList(trains, elements);
        ObservableList<String> items = new CompositeObservableList<>(lists);
        FilteredList<String> textFilteredItems = items.filtered(null);
        ObservableValue<Predicate<String>> textFilterBinding = Bindings.createObjectBinding(() -> {
            String text = filterText.getText().trim().toLowerCase();
            return s -> s.toLowerCase().contains(text);
        }, filterText.textProperty());
        listeners.get(context).add(textFilterBinding);
        textFilteredItems.predicateProperty().bind(textFilterBinding);

        elementList.setItems(textFilteredItems);
        // legend.setCellFactory();
    }

    void setDataSource(@Nonnull DataSource source) {
        switch (source.getType()) {
            case LOG_FILE:
                Context context = null;
                try {
                    context = new GraphParser(source.getUri().getFile()).parse();
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
                listeners.put(context, new LinkedList<>());
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
            case DATABASE:
                Database db;
                try {
                    db = new Database(source.getUri());
                    db.testConnection();
                } catch (SQLException e) {
                    if (e.getMessage().contains("ACCESS_DENIED")) {
                        showLoginWindow();
                    } else {
                        System.out.println(e.getMessage());
                    }
                }
            default:
                return;
        }

        showElements();
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
            centerPane.getChildren().add(rootGroup);
            showLegend();
            graph.getElements().values()
                    .forEach(element -> {
                        ObservableValue<LegendItem.State> state = legendStates.get(GraphObject.element(element.getRepresented().getType()));
                        Binding<Boolean> binding = Bindings.createBooleanBinding(() -> state.getValue() != LegendItem.State.DISABLED, state);
                        listeners.get(context).add(binding);
                        element.getShape().visibleProperty().bind(binding);
                    });
        }
        return graph;
    }

    private void addToCenter(Collection<? extends GraphShape<?>> values) {
        ObservableList<Node> children = centerPane.getChildren();
        for (GraphShape<?> graphShape : values) {
            Shape shape = graphShape.getShape();
            //children.add(shape);
            rootGroup.getChildren().add(shape);
        }
    }

    private void fitGraphToCenter(Graph graph) {
        Bounds graphBounds = graph.getBounds();
        double widthFactor = (centerPane.getWidth()) / graphBounds.getWidth();
        double heightFactor = (centerPane.getHeight()) / graphBounds.getHeight();

        double scaleFactor = Math.min(widthFactor, heightFactor);

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

    private void showLoginWindow() {
        graph = null;
        ContextHolder.getInstance().setContext(null);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginWindow.fxml"));
        loader.setResources(ResourceBundle.getBundle("bundles.localization"));
        try {
            loader.load();
        } catch (IOException e) {
            // This should never happen, because the location is set (see load function)
            return;
        }
        LoginController controller = loader.getController();
        controller.setStage(stage);
    }
}
