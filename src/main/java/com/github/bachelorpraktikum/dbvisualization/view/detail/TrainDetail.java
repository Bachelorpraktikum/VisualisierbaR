package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;

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
    Coordinates getCoordinates() {
        // TODO: Decide on which position to display
        return getState().getPosition().getFrontEdge().getNode1().getCoordinates();
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
}
