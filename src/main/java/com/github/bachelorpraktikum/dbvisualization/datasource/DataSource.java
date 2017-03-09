package com.github.bachelorpraktikum.dbvisualization.datasource;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import java.io.Closeable;

public interface DataSource extends Closeable {

    Context getContext();
}
