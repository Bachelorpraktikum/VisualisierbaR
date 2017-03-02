package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.CompositeObservableList;
import com.github.bachelorpraktikum.dbvisualization.DataSource;
import com.github.bachelorpraktikum.dbvisualization.FXCollectors;
import com.github.bachelorpraktikum.dbvisualization.database.Database;
import com.github.bachelorpraktikum.dbvisualization.logparser.GraphParser;
import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Event;
import com.github.bachelorpraktikum.dbvisualization.model.GraphObject;
import com.github.bachelorpraktikum.dbvisualization.model.Shapeable;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import com.github.bachelorpraktikum.dbvisualization.view.detail.ElementDetailBase;
import com.github.bachelorpraktikum.dbvisualization.view.detail.ElementDetailController;
import com.github.bachelorpraktikum.dbvisualization.view.graph.Graph;
import com.github.bachelorpraktikum.dbvisualization.view.graph.GraphShape;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.ProportionalCoordinatesAdapter;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.SimpleCoordinatesAdapter;
import com.github.bachelorpraktikum.dbvisualization.view.legend.LegendListViewCell;
import com.github.bachelorpraktikum.dbvisualization.view.train.TrainView;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Group;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MainController {

    @FXML
    private AnchorPane detail;
    @FXML
    private ToggleButton eventTraversal;

    @FXML
    private ListView<GraphObject> elementList;
    @FXML
    private CheckBox elementFilter;
    @FXML
    private CheckBox trainFilter;
    @FXML
    private TextField filterText;
    @FXML
    private ToggleButton proportionalToggle;

    @FXML
    private ListView<Shapeable> legend;
    @FXML
    private ToggleButton legendButton;
    @FXML
    private TextField velocityText;
    @FXML
    private ToggleButton playToggle;
    @FXML
    private Button closeButton;
    @FXML
    private BorderPane rootPane;

    @FXML
    private ElementDetailController detailBoxController;
    @FXML
    private Button closeDetailButton;
    @FXML
    private Pane leftPane;
    @FXML
    private ToggleButton logToggle;
    @FXML
    private ListView<Event> logList;
    @FXML
    private Button resetButton;
    @FXML
    private TextField timeText;
    @FXML
    private HBox rightSpacer;

    @FXML
    private Pane centerPane;
    @FXML
    private Pane graphPane;
    @Nullable
    private Graph graph;
    private Map<Train, TrainView> trains;
    @Nullable
    private Highlightable lastHighlighted = null;

    private Stage stage;

    private static final double SCALE_DELTA = 1.1;

    private double mousePressedX = -1;
    private double mousePressedY = -1;

    private boolean autoChange = false;
    private Pattern timePattern;

    private IntegerProperty simulationTime;
    private IntegerProperty velocity;
    private Animation simulation;
    private Timeline eventTraversalTimeline;

    @FXML
    private void initialize() {
        timePattern = Pattern.compile("(\\d+)(m?s?|h)?$");
        trains = new WeakHashMap<>();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        // START OF TIME RELATED INIT

        simulationTime = new SimpleIntegerProperty();
        simulationTime.addListener((observable, oldValue, newValue) -> {
            if (ContextHolder.getInstance().hasContext()) {
                Context context = ContextHolder.getInstance().getContext();
                timeText.setText(String.format("%dms", newValue.intValue()));
                Element.in(context).setTime(newValue.intValue());
            }
        });

        timeText.setOnAction(event -> {
            String text = timeText.getText();
            Matcher timeMatch = timePattern.matcher(text);
            int newTime = 0;

            if (timeMatch.find()) {
                try {
                    newTime = getMsFromString(text);
                } catch (NumberFormatException e) {
                    timeText.setText(simulationTime.get() + "ms");
                    return;
                }
            }

            simulationTime.set(newTime);
            selectClosestLogEntry(newTime);
        });
        timeText.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                timeText.setText(simulationTime.get() + "ms");
            }
        });

        this.simulation = new Timeline(new KeyFrame(Duration.millis(50), event -> {
            int time = (int) (simulationTime.get() + (velocity.get() * 0.05));
            simulationTime.set(time);
            selectClosestLogEntry(time);
        }));
        simulation.setCycleCount(Animation.INDEFINITE);

        eventTraversalTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            Event nextEvent = selectNextEvent(getCurrentTime());
            simulationTime.set(nextEvent.getTime());
        }));
        eventTraversal.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                eventTraversalTimeline.play();
            } else {
                eventTraversalTimeline.stop();
            }
            timeText.setDisable(newValue);
            playToggle.setDisable(newValue);
        });
        eventTraversalTimeline.setCycleCount(Timeline.INDEFINITE);

        // END OF TIME RELATED INIT

        fireOnEnterPress(closeButton);
        fireOnEnterPress(logToggle);
        closeButton.setOnAction(event -> showSourceChooser());
        resetButton.setOnAction(event -> {
            simulationTime.set(Context.INIT_STATE_TIME);
            selectClosestLogEntry(Context.INIT_STATE_TIME);
        });

        proportionalToggle.setOnAction(ActionEvent -> switchGraph());

        legendButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            legend.setVisible(newValue);
            if (newValue) {
                legend.toFront();
            } else {
                legend.toBack();
            }
        });
        closeDetailButton.setOnAction(event -> hideDetailView());

        // Hide logList by default
        rootPane.setLeft(null);
        logToggle.selectedProperty().addListener((observable, oldValue, newValue) ->
            rootPane.setLeft(newValue ? leftPane : null)
        );

        initializeElementList();
        initializeLogList();
        initializeCenterPane();

        velocity = new SimpleIntegerProperty(1000);
        velocityText.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int newVelocity = Integer.parseUnsignedInt(newValue);
                velocity.set(newVelocity);
            } catch (NumberFormatException e) {
                velocityText.setText(oldValue);
            }
        });

        playToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                simulation.playFromStart();
            } else {
                simulation.stop();
            }
            timeText.setDisable(newValue);
            eventTraversal.setDisable(newValue);
        });

        detailBoxController.setCenterPane(centerPane);
    }

    private void initializeCenterPane() {
        ChangeListener<Number> boundsListener = (observable, oldValue, newValue) -> {
            if (ContextHolder.getInstance().hasContext()) {
                fitGraphToCenter(getGraph());
            }
        };
        graphPane.heightProperty().addListener(boundsListener);
        graphPane.widthProperty().addListener(boundsListener);
        graphPane.setOnScroll(event -> {
            if (graph != null) {
                Group group = graph.getGroup();
                Bounds bounds = group.localToScene(group.getBoundsInLocal());
                double oldScale = group.getScaleX();
                double scaleFactor =
                    oldScale * ((event.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA);
                double translateX =
                    event.getScreenX() - (bounds.getWidth() / 2 + bounds.getMinX());
                double translateY =
                    event.getScreenY() - (bounds.getHeight() / 2 + bounds.getMinY());
                double factor = (scaleFactor / oldScale) - 1;

                group.setScaleX(scaleFactor);
                group.setScaleY(scaleFactor);
                group.setTranslateX(group.getTranslateX() - factor * translateX);
                group.setTranslateY(group.getTranslateY() - factor * translateY);
            }
        });
        graphPane.setOnMouseReleased(event -> {
            mousePressedX = -1;
            mousePressedY = -1;
        });
        graphPane.setOnMouseDragged(event -> {
            if (!event.isPrimaryButtonDown()) {
                return;
            }

            if (mousePressedX == -1 && mousePressedY == -1) {
                mousePressedX = event.getX();
                mousePressedY = event.getY();
            }

            double xOffset = (event.getX() - mousePressedX);
            double yOffset = (event.getY() - mousePressedY);

            graphPane.setTranslateX(graphPane.getTranslateX() + xOffset);
            graphPane.setTranslateY(graphPane.getTranslateY() + yOffset);
            event.consume();
        });
    }

    private void initializeLogList() {
        StringConverter<Event> stringConverter = new StringConverter<Event>() {
            @Override
            public String toString(Event event) {
                return event.getDescription();
            }

            @Override
            public Event fromString(String string) {
                return null;
            }
        };

        Callback<ListView<Event>, ListCell<Event>> textFactory = TextFieldListCell
            .forListView(stringConverter);

        AtomicReference<Binding<Paint>> paintBinding = new AtomicReference<>();
        Callback<ListView<Event>, ListCell<Event>> listCellFactory = cell -> {
            ListCell<Event> result = textFactory.call(cell);
            Paint defaultTextFill = result.getTextFill();
            result.itemProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    Binding<Paint> warningBinding = Bindings.createObjectBinding(
                        () -> newValue.getWarnings().isEmpty() ?
                            defaultTextFill : Color.rgb(255, 0, 0),
                        newValue.getWarnings()
                    );
                    paintBinding.set(warningBinding);
                    result.textFillProperty().bind(warningBinding);
                }
            }));

            TooltipUtil.install(result, () -> {
                StringBuilder sb = new StringBuilder();
                if (result.getItem() != null) {
                    sb.append(result.getItem().getDescription());
                    for (String warning : result.getItem().getWarnings()) {
                        sb.append(System.lineSeparator()).append("[WARN]: ").append(warning);
                    }
                }
                return sb.toString();
            });

            return result;
        };

        logList.setCellFactory(listCellFactory);
        logList.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (!autoChange) {
                    simulationTime.set(newValue.getTime());
                }
                Element.in(ContextHolder.getInstance().getContext()).setTime(newValue.getTime());
            }
        );
    }

    private void initializeElementList() {
        StringConverter<GraphObject> stringConverter = new StringConverter<GraphObject>() {
            @Override
            public String toString(GraphObject object) {
                return object.getName();
            }

            @Override
            public GraphObject fromString(String string) {
                return null;
            }
        };
        Callback<ListView<GraphObject>, ListCell<GraphObject>> textFactory = TextFieldListCell
            .forListView(stringConverter);
        Callback<ListView<GraphObject>, ListCell<GraphObject>> elementListCellFactory =
            (listView) -> {
                ListCell<GraphObject> cell = textFactory.call(listView);
                TooltipUtil.install(cell, () -> {
                    if (cell.getItem() != null) {
                        return cell.getItem().getName();
                    } else {
                        return "";
                    }
                });
                return cell;
            };

        elementList.setCellFactory(elementListCellFactory);
        elementList.getSelectionModel().selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (lastHighlighted != null) {
                    lastHighlighted.highlightedProperty().set(false);
                }

                if (newValue == null) {
                    return;
                }

                if (newValue instanceof Element) {
                    Element elemnt = (Element) newValue;
                    lastHighlighted = getGraph().getElements().get(elemnt);
                } else {
                    Train train = (Train) newValue;
                    lastHighlighted = trains.get(train);
                }
                lastHighlighted.highlightedProperty().set(true);

                setDetail(ElementDetailBase.create(newValue, simulationTime));
            });
    }


    private int getLastEventIndex(int time) {
        int last = 0;
        List<Event> items = logList.getItems();
        for (int index = 0; index < items.size(); index++) {
            Event item = items.get(index);
            if (item.getTime() > time) {
                return last;
            }
            last = index;
        }
        // Return last
        return last;
    }

    private Event selectNextEvent(int time) {
        int index = getLastEventIndex(time) + 1;
        if (index >= logList.getItems().size()) {
            index--;
        }

        Event event = logList.getItems().get(index);
        selectEvent(event);
        return event;
    }

    private Event selectClosestLogEntry(int time) {
        Event event = logList.getItems().get(getLastEventIndex(time));
        selectEvent(event);
        return event;
    }

    private void selectEvent(Event event) {
        autoChange = true;
        logList.getSelectionModel().select(event);
        logList.scrollTo(event);
        autoChange = false;
    }

    private int getMsFromString(String timeString) {
        int ms = -1;

        Matcher timeMatch = timePattern.matcher(timeString);
        String type = "ms";
        int time = ms;

        if (timeMatch.find()) {
            String typeMatch = timeMatch.group(2);
            if (typeMatch != null) {
                type = typeMatch;
            }

            time = Integer.valueOf(timeMatch.group(1));
        }

        switch (type) {
            case "s":
                ms = time * 1000;
                break;
            case "m":
                ms = time * 1000 * 60;
                break;
            case "h":
                ms = time * 1000 * 60 * 60;
                break;
            default:
                ms = time;
        }

        return ms;
    }

    private void showDetailView() {
        detail.toFront();
        legend.toBack();
        elementList.toBack();
    }

    private void hideDetailView() {
        detail.toBack();
        elementList.toFront();
        elementList.getSelectionModel().clearSelection();
    }

    /**
     * Adds an EventHandler to the button which fires the button on pressing enter.
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
        stage.setMaximized(false);
        stage.setMaximized(true);
    }

    private void showLegend() {
        Context context = ContextHolder.getInstance().getContext();
        ObservableList<Shapeable> items = Stream.concat(
            Train.in(context).getAll().stream(),
            Element.in(context).getAll().stream()
                .map(Element::getType)
                .distinct()
        ).collect(FXCollectors.toObservableList());

        legend.setItems(items);
        legend.setCellFactory(studentListView -> new LegendListViewCell());
    }

    private int getCurrentTime() {
        return simulationTime.get();
    }

    private void showElements() {
        Context context = ContextHolder.getInstance().getContext();

        FilteredList<Train> trains = FXCollections.observableList(
            new ArrayList<>(Train.in(context).getAll())
        ).filtered(null);
        ObservableValue<Predicate<Train>> trainBinding = Bindings.createObjectBinding(
            () -> s -> trainFilter.isSelected(),
            trainFilter.selectedProperty()
        );
        context.addObject(trainBinding);
        trains.predicateProperty().bind(trainBinding);

        FilteredList<Element> elements = FXCollections.observableList(
            new ArrayList<>(Element.in(context).getAll())
        ).filtered(null);
        ObservableValue<Predicate<Element>> elementBinding = Bindings.createObjectBinding(
            () -> s -> elementFilter.isSelected(),
            elementFilter.selectedProperty());
        context.addObject(elementBinding);
        elements.predicateProperty().bind(elementBinding);

        ObservableList<GraphObject> items = new CompositeObservableList<>(trains, elements);
        FilteredList<GraphObject> textFilteredItems = items.filtered(null);
        ObservableValue<Predicate<GraphObject>> textFilterBinding = Bindings
            .createObjectBinding(() -> {
                String text = filterText.getText().trim().toLowerCase();
                return s -> s.getName().toLowerCase().contains(text);
            }, filterText.textProperty());
        context.addObject(textFilterBinding);
        textFilteredItems.predicateProperty().bind(textFilterBinding);

        elementList.setItems(textFilteredItems);
    }

    void setDataSource(@Nonnull DataSource source) {
        switch (source.getType()) {
            case LOG_FILE:
                Context context = null;
                try {
                    context = new GraphParser(source.getUri().toURL().getFile()).parse();
                } catch (IOException | RuntimeException e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    String headerText = ResourceBundle.getBundle("bundles.localization")
                        .getString("parse_error_header");
                    alert.setHeaderText(headerText);
                    String contentText = ResourceBundle.getBundle("bundles.localization")
                        .getString("parse_error_content");
                    alert.setContentText(contentText);
                    alert.show();
                    showSourceChooser();
                    return;
                }
                ContextHolder.getInstance().setContext(context);
                logList.setItems(context.getObservableEvents().sorted());
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
                return;
            default:
                return;
        }

        simulationTime.set(Context.INIT_STATE_TIME);
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
            if (proportionalToggle.isSelected()) {
                graph = new Graph(context, new ProportionalCoordinatesAdapter(context));
            } else {
                graph = new Graph(context, new SimpleCoordinatesAdapter());
            }
            graphPane.getChildren().add(graph.getGroup());
            showLegend();

            for (Map.Entry<Element, GraphShape<Element>> entry : graph.getElements()
                .entrySet()) {
                Element element = entry.getKey();
                Shape elementShape = entry.getValue().getShape(element);
                Binding<Boolean> binding = Bindings.createBooleanBinding(() ->
                        element.getType().isVisible(elementShape.getBoundsInParent()),
                    element.getType().visibleStateProperty()
                );
                context.addObject(binding);
                entry.getValue().getShape(element).visibleProperty().bind(binding);
                elementShape.setOnMouseClicked(event -> {
                    elementList.getSelectionModel().select(element);
                });
            }
            for (Train train : Train.in(context).getAll()) {
                TrainView trainView = new TrainView(train, graph);
                trainView.timeProperty().bind(simulationTime);
                trains.put(train, trainView);
                trainView.setOnMouseClicked(e -> elementList.getSelectionModel().select(train));
            }
        }
        return graph;
    }

    private void setDetail(ElementDetailBase detail) {
        showDetailView();
        detailBoxController.setDetail(detail);
    }

    private void fitGraphToCenter(Graph graph) {
        Bounds graphBounds = graph.getGroup().getBoundsInParent();
        double widthFactor = (graphPane.getWidth()) / graphBounds.getWidth();
        double heightFactor = (graphPane.getHeight()) / graphBounds.getHeight();

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
        Bounds graphBounds = graph.getGroup().getBoundsInParent();

        double finalX = (graphPane.getWidth() - graphBounds.getWidth()) / 2;
        double xTranslate = finalX - graphBounds.getMinX();

        double finalY = (graphPane.getHeight() - graphBounds.getHeight()) / 2;
        double yTranslate = finalY - graphBounds.getMinY();

        graph.move(xTranslate, yTranslate);
    }

    private void showSourceChooser() {
        stage.setMaximized(false);
        if (graph != null) {
            simulation.stop();
            graphPane.getChildren().clear();
            graph = null;
        }
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

    private void switchGraph() {
        graphPane.getChildren().clear();
        graph = null;
        fitGraphToCenter(getGraph());
    }
}
