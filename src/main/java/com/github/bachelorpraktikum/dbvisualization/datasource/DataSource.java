package com.github.bachelorpraktikum.dbvisualization.datasource;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import java.io.Closeable;
import javax.annotation.Nonnull;

/**
 * <p>Represents a source of graph data.</p>
 *
 * <p>The source will probably be some form of model output.</p>
 *
 * <p>Please note that the {@link #close()} method should be called before disposing a data source
 * object.</p>
 */
public interface DataSource extends Closeable {

    /**
     * Gets the context with which the data provided by this data source is associated.
     *
     * @return a context
     */
    @Nonnull
    Context getContext();
}
