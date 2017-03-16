package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.config.ConfigFile;
import com.github.bachelorpraktikum.dbvisualization.config.ConfigKey;
import com.github.bachelorpraktikum.dbvisualization.datasource.RestSource;
import com.github.bachelorpraktikum.dbvisualization.model.Event;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train.State;
import com.github.bachelorpraktikum.dbvisualization.view.ContextMenuUtil;
import com.github.bachelorpraktikum.dbvisualization.view.DataSourceHolder;
import com.github.bachelorpraktikum.dbvisualization.view.Exporter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Shape;
import javafx.stage.FileChooser;

public class ElementDetailController {

    @FXML
    private VBox detailView;
    @FXML
    private Label coordinateLabel;
    @FXML
    private Label coordinateValueBack;
    @FXML
    private VBox elementBox;
    @FXML
    private Label stateValue;
    @FXML
    private Label typeValue;
    @FXML
    private Button breakButton;
    @FXML
    private Label elementName;
    @FXML
    private Group elementImage;
    @FXML
    private Label coordinateValue;
    @FXML
    private LineChart<Double, Double> vtChart;
    @FXML
    private LineChart<Double, Double> vdChart;
    @FXML
    private LineChart<Double, Double> dtChart;
    @FXML
    private VBox trainBox;
    @FXML
    private Label speedValue;
    private ElementDetailBase detail;
    @FXML
    private Label lengthValue;

    private List<Object> bindings;


    private static final Function<State, Double> DISTANCE = s -> {
        double distance = s.getTotalDistance() / 1000.0;
        return distance < 0 ? 0 : distance;
    };
    private static final Function<State, Double> TIME = s -> {
        double time = s.getTime() / 1000.0;
        return time < 0 ? 0 : time;
    };

    private enum ChartType {
        vt("title_vt", "s", "m/s", TIME, State::getSpeed, true),
        vd("title_vd", "km", "m/s", DISTANCE, State::getSpeed, true),
        dt("title_dt", "s", "km", TIME, DISTANCE, false);

        private final String titleKey;
        private final String xName;
        private final String yName;
        private final Function<State, Double> xFunction;
        private final Function<State, Double> yFunction;
        private final boolean isSpeedOnYAxis;


        ChartType(String titleKey,
            String xName,
            String yName,
            Function<State, Double> xFunction,
            Function<State, Double> yFunction,
            boolean isSpeedOnYAxis) {
            this.titleKey = titleKey;
            this.xName = xName;
            this.yName = yName;
            this.xFunction = xFunction;
            this.yFunction = yFunction;
            this.isSpeedOnYAxis = isSpeedOnYAxis;
        }

        private ResourceBundle getResourceBundle() {
            return ResourceBundle.getBundle("bundles.localization");
        }

        public String getTitle() {
            return getResourceBundle().getString(titleKey);
        }

        public String getXAxisName() {
            return xName;
        }

        public String getYAxisName() {
            return yName;
        }

        public boolean isSpeedOnYAxis() {
            return isSpeedOnYAxis;
        }

        public Function<State, Double> getXFunction() {
            return xFunction;
        }

        public Function<State, Double> getYFunction() {
            return yFunction;
        }
    }

    private Map<ChartType, LineChart<Double, Double>> charts;
    private Map<ChartType, ObservableList<Data<Double, Double>>> chartData;
    private LineChart<Double, Double> bigChart;
    private ChartType currentBigChart;

    @FXML
    private void initialize() {
        bindings = new LinkedList<>();

        charts = new EnumMap<>(ChartType.class);
        charts.put(ChartType.vt, vtChart);
        charts.put(ChartType.vd, vdChart);
        charts.put(ChartType.dt, dtChart);

        bigChart = createChart();
        bigChart.setVisible(false);
        bigChart.setOnMouseClicked(event -> bigChart.setVisible(false));

        chartData = new EnumMap<>(ChartType.class);
        for (ChartType type : ChartType.values()) {
            ObservableList<Data<Double, Double>> data = FXCollections.observableList(
                new ArrayList<>(256)
            );
            chartData.put(type, data);
            LineChart<Double, Double> chart = charts.get(type);
            chart.setData(FXCollections.singletonObservableList(new Series<>(data)));
            chart.setTitle(type.getTitle());
            chart.getXAxis().setLabel(type.getXAxisName());
            chart.getYAxis().setLabel(type.getYAxisName());

            registerMagnifier(type);
            MenuItem exportItem = new MenuItem("Export");
            exportItem.setOnAction(event -> export(type));
            ContextMenuUtil.attach(chart, Collections.singletonList(exportItem));
        }

        DataSourceHolder dataSourceHolder = DataSourceHolder.getInstance();
        BooleanBinding isRestSource = dataSourceHolder.presentProperty().and(
            Bindings.createBooleanBinding(
                () -> dataSourceHolder.get() instanceof RestSource,
                dataSourceHolder
            )
        );
        bindings.add(isRestSource);
        breakButton.visibleProperty().bind(isRestSource);
        breakButton.setOnAction(event -> {
            breakButton.setDisable(true);
            RestSource dataSource = (RestSource) DataSourceHolder.getInstance().get();
            ElementDetail elementDetail = (ElementDetail) detail;
            dataSource.breakElement(
                elementDetail.getElement(),
                () -> breakButton.setDisable(false)
            );
        });
    }

    private LineChart<Double, Double> createChart() {
        URL location = ElementDetailController.class.getResource("LineChart.fxml");
        FXMLLoader loader = new FXMLLoader(location);
        try {
            return loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void registerMagnifier(ChartType type) {
        charts.get(type).setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (currentBigChart == type) {
                    bigChart.setData(FXCollections.emptyObservableList());
                    bigChart.setVisible(false);
                    currentBigChart = null;
                } else {
                    Series<Double, Double> data = new Series<>(chartData.get(type));
                    bigChart.setData(FXCollections.singletonObservableList(data));
                    bigChart.setTitle(type.getTitle());
                    bigChart.getXAxis().setLabel(type.getXAxisName());
                    bigChart.getYAxis().setLabel(type.getYAxisName());
                    bigChart.setVisible(true);
                    currentBigChart = type;
                }
            }
        });
    }

    private void resetCharts() {
        for (ChartType type : ChartType.values()) {
            chartData.get(type).clear();
        }
    }

    public void setCenterPane(Pane center) {
        if (!center.getChildren().contains(bigChart)) {
            center.getChildren().add(0, bigChart);
            bigChart.visibleProperty().addListener(
                (observable, oldValue, newValue) ->
                    center.getChildren().get(1).setVisible(!newValue)
            );
        }
    }

    public void setDetail(ElementDetailBase detail) {
        bindings.clear();
        resetCharts();
        if (detail == null) {
            return;
        }

        this.detail = detail;

        trainBox.setVisible(detail.isTrain());
        elementBox.setVisible(!detail.isTrain());

        elementName.textProperty().setValue(detail.getName());

        Binding<String> coordBinding = Bindings.createStringBinding(() ->
                String.valueOf(detail.getCoordinatesString(detail.getCoordinates())),
            detail.timeProperty()
        );
        bindings.add(coordBinding);
        coordinateValue.textProperty().bind(coordBinding);

        Shape shape = detail.getShape();

        if (detail.isTrain()) {
            TrainDetail trainDetail = (TrainDetail) detail;
            Binding<String> backCoordBinding = Bindings.createStringBinding(() ->
                    detail.getCoordinatesString(trainDetail.getBackCoordinate()),
                detail.timeProperty()
            );
            bindings.add(backCoordBinding);
            coordinateValueBack.textProperty().bind(backCoordBinding);

            coordinateLabel.setText(
                ResourceBundle.getBundle("bundles.localization").getString("coordinate_front")
            );
            lengthValue.textProperty().setValue(String.format("%dm", trainDetail.getLength()));

            Binding<String> speedBinding = Bindings.createStringBinding(() ->
                    String.format("%fm/s", trainDetail.getSpeed()),
                trainDetail.timeProperty()
            );
            bindings.add(speedBinding);
            speedValue.textProperty().bind(speedBinding);

            updateCharts(detail.timeProperty().get(), Integer.MAX_VALUE);
            ChangeListener<Number> chartListener = ((observable, oldValue, newValue) ->
                updateCharts(newValue.intValue(), oldValue.intValue())
            );
            bindings.add(chartListener);
            detail.timeProperty().addListener(new WeakChangeListener<>(chartListener));
        } else {
            ElementDetail elementDetail = (ElementDetail) detail;
            typeValue.setText(elementDetail.getElement().getType().getName());

            Binding<String> stateBinding = Bindings.createStringBinding(() ->
                    String.valueOf(((ElementDetail) detail).getState()),
                detail.timeProperty()
            );
            bindings.add(stateBinding);
            stateValue.textProperty().bind(stateBinding);
        }

        if (!elementImage.getChildren().contains(shape)) {
            if (elementImage.getChildren().size() > 0) {
                elementImage.getChildren().remove(0);
            }
        }

        if (shape != null) {
            resizeShape(shape, 20);
            elementImage.getChildren().add(0, shape);
        }
    }

    private void resizeShape(Shape shape, double max) {
        Bounds shapeBounds = shape.getBoundsInParent();
        double maxShape = Math.max(shapeBounds.getWidth(), shapeBounds.getHeight());
        double factor = max / maxShape;
        shape.setScaleX(shape.getScaleX() * factor);
        shape.setScaleY(shape.getScaleY() * factor);
        shape.setRotate(180);
    }

    private void updateCharts(int time, int previousTime) {
        if (!detail.isTrain()) {
            return;
        }

        if (previousTime > time) {
            resetCharts();
        }

        for (ChartType type : ChartType.values()) {
            updateChart(type, time, previousTime);
        }
    }

    private void updateChart(ChartType type, int time, int previousTime) {
        Train train = (Train) detail.getElement();

        ObservableList<Data<Double, Double>> data = chartData.get(type);
        Function<State, Double> xFunction = type.getXFunction();
        Function<State, Double> yFunction = type.getYFunction();

        State state = null;
        if (previousTime > time) {
            previousTime = Integer.MIN_VALUE;
            state = train.getState(0);
            data.add(new Data<>(xFunction.apply(state), yFunction.apply(state)));
        } else if (!data.isEmpty()) {
            data.remove(data.size() - 1);
            if (!data.isEmpty()) {
                state = (State) data.get(data.size() - 1).getExtraValue();
            }
        }

        for (Event event : train.getEvents()) {
            if (event.getTime() > time) {
                break;
            }
            if (event.getTime() < 0 || event.getTime() <= previousTime) {
                continue;
            }
            State newState = train.getState(event.getTime(), state);
            if (state != null && type.isSpeedOnYAxis()) {
                Double x = xFunction.apply(newState);
                Double y = yFunction.apply(state);
                data.add(new Data<>(x, y));
            }
            state = newState;
            data.add(new Data<>(xFunction.apply(state), yFunction.apply(state), state));
        }
        state = train.getState(time, state);
        data.add(new Data<>(xFunction.apply(state), yFunction.apply(state)));
    }

    private void export(ChartType type) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters()
            .addAll(new FileChooser.ExtensionFilter("gnuplot (*.dat)", "*.dat"),
                new FileChooser.ExtensionFilter("PNG Image (*.png)", "*.png"),
                new FileChooser.ExtensionFilter("JPEG Image (*.jpg)", "*.jpg"));
        String initDirString = ConfigFile.getInstance().getProperty(
            ConfigKey.initialLogFileDirectory.getKey(),
            System.getProperty("user.home")
        );
        File initDir = new File(initDirString);
        fileChooser.setInitialDirectory(initDir);
        fileChooser.setInitialFileName(type.getTitle());

        File file = fileChooser.showSaveDialog(trainBox.getScene().getWindow());

        if (file != null) {
            LineChart<Double, Double> chart = charts.get(type);
            Exporter.exportTrainDetail(chart, file);
        }
    }
}
