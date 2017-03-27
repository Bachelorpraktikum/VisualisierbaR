package com.github.bachelorpraktikum.dbvisualization.model.train;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Shapeable;
import com.github.bachelorpraktikum.dbvisualization.model.ShapeableImplementationTest;

public class TrainShapeableTest extends ShapeableImplementationTest {

    @Override
    protected Shapeable<?> getShapeable() {
        Context context = new Context();
        return Train.in(context).create("train", "t", 10);
    }
}
