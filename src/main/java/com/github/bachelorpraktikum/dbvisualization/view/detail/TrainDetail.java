package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train.State;
import javafx.beans.property.IntegerProperty;
import javafx.geometry.Point2D;
import javax.annotation.Nullable;

public class TrainDetail extends ElementDetailBase<Train> {

    public TrainDetail(Train train, IntegerProperty time) {
        super(train, time);
    }

    @Override
    String getName() {
        return getElement().getReadableName();
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
        return getElement().getState(timeProperty().get());
    }

    double getSpeed() {
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
