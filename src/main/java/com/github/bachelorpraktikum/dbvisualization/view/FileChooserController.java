package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.DataSource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;


public class FileChooserController implements SourceChooser {
    @FXML
    private TextField pathField;
    @FXML
    private GridPane rootPane;

    @FXML
    private Button explorerButton;
    private FileChooser fileChooser;

    private ReadOnlyObjectWrapper<URL> fileURLProperty;

    @FXML
    private void initialize() {
        fileURLProperty = new ReadOnlyObjectWrapper<>();
        fileChooser = new FileChooser();
        explorerButton.setOnAction(event -> updatePath(openFileChooser()));

        pathField.textProperty().addListener((o, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                fileURLProperty.set(null);
                return;
            }
            try {
                URL url = new File(newValue).getAbsoluteFile().toURI().toURL();
                fileURLProperty.set(url);
            } catch (MalformedURLException e) {
                // ignore.
                // This won't ever happen, because File.toURI().toURL() won't ever create an URL with an invalid protocol.
            }
        });
    }

    /**
     * Opens the system explorer where a file can be chosen.
     * If a file was chosen the {@link File file} will be returned.
     * If no file was chosen and the explorer has been closed, null will be returned.
     *
     * @return A {@link File file} or null
     */
    @Nonnull
    private File openFileChooser() {
        return fileChooser.showOpenDialog(rootPane.getScene().getWindow());
    }

    /**
     * Updates the text in the path field with the absolute path of the <code>file</code>.
     *
     * @param file File to get the path from
     */
    private void updatePath(@Nullable File file) {
        if (file == null) {
            return;
        }

        pathField.setText(file.getAbsolutePath());
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public URL getResourceURL() {
        return fileURLProperty.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ReadOnlyObjectProperty<URL> resourceURLProperty() {
        return fileURLProperty.getReadOnlyProperty();
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
        return DataSource.Type.LOG_FILE;
    }
}
