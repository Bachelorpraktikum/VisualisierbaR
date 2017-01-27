package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import javafx.geometry.Point2D;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TrainDetail extends ElementDetailBase {
    private Train train;

    public TrainDetail(Train train) {
        this.train = train;
    }

    @Override
    String getName() {
        return train.getReadableName();
    }

    @Override
    List<URL> getImageUrls() {
        List<URL> urls = new ArrayList<URL>();
        urls.add(Train.class.getResource(String.format("../symbols/%s.fxml", "train")));

        return urls;
    }

    @Override
    Point2D getCoordinates() {
        // TODO: ALSO DISPLAY BACK COORDINATES
        return getState().getPosition().getFrontCoordinates();
    }

    @Override
    boolean isTrain() {
        return true;
    }

    Train.State getState() {
        return train.getState(getTime());
    }

    int getSpeed() {
        return getState().getSpeed();
    }

    int getLength() {
        return train.getLength();
    }

    String getBackCoordinate() {
        Point2D coord = train.getState(getTime()).getPosition().getBackCoordinates();

        return String.format("x: %f | y: %f", coord.getX(), coord.getY());
    }
}
