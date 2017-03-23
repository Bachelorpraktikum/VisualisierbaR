package com.github.bachelorpraktikum.dbvisualization.view.detail;


import com.github.bachelorpraktikum.dbvisualization.datasource.DataSource;
import com.github.bachelorpraktikum.dbvisualization.datasource.LiveTrain;
import com.github.bachelorpraktikum.dbvisualization.datasource.RestSource;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import com.github.bachelorpraktikum.dbvisualization.view.DataSourceHolder;
import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javax.annotation.Nullable;

public class RestTrainDetailController {

    @FXML
    private Label v;
    @FXML
    private Label accelState;
    @FXML
    private Label emergCount;
    @FXML
    private Label fahrCount;

    private Property<Train> train;
    private InvalidationListener listener;

    @FXML
    private void initialize() {
        train = new SimpleObjectProperty<>();
        listener = observable -> refresh();

        DataSourceHolder.getInstance().addListener(listener);
        DataSourceHolder.getInstance().addListener((observable, oldValue, newValue) -> {
            registerTimeListener(newValue);
        });
        train.addListener(listener);
        registerTimeListener(DataSourceHolder.getInstance().getNullable());
        refresh();
    }

    private void registerTimeListener(@Nullable DataSource dataSource) {
        if (dataSource instanceof RestSource) {
            RestSource restSource = (RestSource) dataSource;
            restSource.timeProperty().addListener(listener);
        }
    }

    private void refresh() {
        Train train = this.train.getValue();
        if (train != null) {
            DataSource dataSource = DataSourceHolder.getInstance().getNullable();
            if (dataSource instanceof RestSource) {
                RestSource restSource = (RestSource) dataSource;
                LiveTrain liveTrain = restSource.getTrain(train);

                v.setText(String.valueOf(liveTrain.getV()));
                accelState.setText(liveTrain.getAccelState());
                emergCount.setText(String.valueOf(liveTrain.getEmergCount()));
                fahrCount.setText(String.valueOf(liveTrain.getFahrCount()));
            }
        }
    }

    public void setTrain(Train train) {
        this.train.setValue(train);
    }
}
