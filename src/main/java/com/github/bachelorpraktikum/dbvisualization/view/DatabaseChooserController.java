package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.DataSource;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.transform.Source;
import java.net.MalformedURLException;
import java.net.URL;

public class DatabaseChooserController implements SourceChooser {
    public BorderPane rootPane;
    public TextField urlField;
    public Label urlError;

    private ReadOnlyObjectWrapper<URL> databaseURLProperty;

    public void initialize() {
        databaseURLProperty = new ReadOnlyObjectWrapper<>();

        urlField.textProperty().addListener((o, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                databaseURLProperty.set(null);
                return;
            }
            boolean isValid = true;
            try {
                URL url = new URL(newValue);
                databaseURLProperty.set(url);
            } catch (MalformedURLException e) {
                isValid = false;
            } finally {
                urlError.setVisible(!isValid);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public URL getResourceURL() {
        return databaseURLProperty.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ReadOnlyObjectProperty<URL> resourceURLProperty() {
        return databaseURLProperty.getReadOnlyProperty();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getRootPaneId() {
        return rootPane.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public DataSource.Type getResourceType() {
        return DataSource.Type.DATABASE;
    }
}
