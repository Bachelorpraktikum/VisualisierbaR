package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.DataSource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

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
            try {
                URL url = new File(String.valueOf(newValue)).getAbsoluteFile().toURI().toURL();
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
    private File openFileChooser() {
        return fileChooser.showOpenDialog(rootPane.getScene().getWindow());
    }

    /**
     * Updates the text in the path field with the absolute path of the <code>file</code>
     *
     * @param file File to get the path from
     */
    private void updatePath(File file) {
        if (file == null) {
            return;
        }

        pathField.setText(file.getAbsolutePath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getResourceURL() {
        return fileURLProperty.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReadOnlyObjectProperty<URL> resourceURLProperty() {
        return fileURLProperty.getReadOnlyProperty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRootPaneId() {
        return rootPane.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSource.Type getResourceType() {
        return DataSource.Type.LOG_FILE;
    }
}
