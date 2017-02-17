package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train.State;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import javax.annotation.Nullable;

public class TrainDetail extends ElementDetailBase<Train> {

    public TrainDetail(Train train) {
        super(train);
    }

    @Override
    String getName() {
        return getElement().getReadableName();
    }

    @Override
    List<URL> getImageUrls() {
        List<URL> urls = new ArrayList<URL>();
        urls.add(Train.class.getResource(String.format("../symbols/%s.fxml", "train")));

        return urls;
    }

    @Nullable
    @Override
    Point2D getCoordinates() {
        State state = getState();

        if (state.isInitialized()) {
            return getState().getPosition().getFrontCoordinates();
        } else {
            return null;
        }
    }

    @Override
    boolean isTrain() {
        return true;
    }

    Train.State getState() {
        return getElement().getState(getTime());
    }

    int getSpeed() {
        return getState().getSpeed();
    }

    int getLength() {
        return getElement().getLength();
    }

    Point2D getBackCoordinate() {
        State state = getState();
        if (state.isInitialized()) {
            return state.getPosition().getBackCoordinates();
        } else {
            return null;
        }
    }
}
