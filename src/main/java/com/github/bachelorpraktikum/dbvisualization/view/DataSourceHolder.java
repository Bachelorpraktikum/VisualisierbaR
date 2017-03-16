package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.datasource.DataSource;
import com.github.bachelorpraktikum.dbvisualization.model.Context;
import java.io.IOException;
import java.util.function.Consumer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Singleton class that stores an instance of {@link DataSource}.
 */
public class DataSourceHolder {

    private static DataSourceHolder instance = new DataSourceHolder();

    /**
     * Gets the singleton instance of this class.
     *
     * @return the singleton DataSourceHolder instance
     */
    public static DataSourceHolder getInstance() {
        return instance;
    }

    private ObjectProperty<DataSource> dataSource;
    private ObservableBooleanValue isPresent;

    private DataSourceHolder() {
        dataSource = new SimpleObjectProperty<>();
        isPresent = dataSource.isNotNull();
        addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                try {
                    oldValue.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Sets the currently active data source.
     *
     * @param dataSource the data source, or null
     */
    void set(@Nullable DataSource dataSource) {
        this.dataSource.set(dataSource);
    }

    /**
     * Determines whether a data source currently exists.
     *
     * @return whether there is a data source
     */
    public boolean isPresent() {
        return isPresent.get();
    }

    public ObservableBooleanValue presentProperty() {
        return isPresent;
    }

    /**
     * Gets the current data source.
     *
     * @return the data source
     * @throws IllegalStateException if there is no data source
     */
    @Nonnull
    public DataSource get() {
        if (dataSource == null) {
            throw new IllegalStateException();
        }
        return dataSource.get();
    }

    @Nonnull
    public Context getContext() {
        return get().getContext();
    }

    public void ifPresent(@Nonnull Consumer<? super DataSource> then) {
        if (isPresent()) {
            then.accept(get());
        }
    }

    public void addListener(@Nonnull ChangeListener<? super DataSource> listener) {
        dataSource.addListener(listener);
    }
}
