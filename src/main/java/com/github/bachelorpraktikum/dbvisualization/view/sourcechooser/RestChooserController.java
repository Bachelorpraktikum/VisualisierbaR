package com.github.bachelorpraktikum.dbvisualization.view.sourcechooser;

import com.github.bachelorpraktikum.dbvisualization.config.ConfigKey;
import com.github.bachelorpraktikum.dbvisualization.datasource.RestSource;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RestChooserController implements SourceChooser<RestSource> {

    private static final String DEFAULT_DIR = ".";
    private static final String DEFAULT_EXECUTABLE = "runRest";

    private static final Logger log = Logger.getLogger(RestChooserController.class.getName());

    @FXML
    private TextField pathField;
    @FXML
    private Pane rootPane;

    @FXML
    private Button explorerButton;
    private FileChooser fileChooser;

    /**
     * Should never have a null value.
     * Also, the value should always be a valid directory.
     */
    private Property<File> chosenDirectory;
    private Property<String> chosenExecutable;
    private ObjectBinding<File> chosenFullPath;
    private BooleanBinding chosenNotNull;

    @FXML
    private void initialize() {
        chosenDirectory = new SimpleObjectProperty<>(getInitialDirectory());
        chosenDirectory.addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ConfigKey.initialRestDirectory.set(newValue.getPath());
            }
        }));
        chosenExecutable = new SimpleObjectProperty<>(getInitialExecutable());
        chosenExecutable.addListener(((observable, oldValue, newValue) ->
            ConfigKey.initialRestExecutable.set(newValue))
        );
        chosenFullPath = Bindings.createObjectBinding(
            () -> {
                File dir = chosenDirectory.getValue();
                String file = chosenExecutable.getValue();
                if (dir == null || file == null) {
                    return null;
                } else {
                    return new File(dir, file);
                }
            }, chosenDirectory, chosenExecutable
        );
        chosenNotNull = chosenFullPath.isNotNull();

        fileChooser = new FileChooser();
        fileChooser.initialDirectoryProperty().bind(chosenDirectory);
        fileChooser.initialFileNameProperty().bind(chosenExecutable);

        pathField.setText(chosenFullPath.get().getPath());
        pathField.textProperty().addListener((observable, oldValue, newValue) -> setPath(newValue));
        pathField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ((Button) rootPane.getScene().lookup("#openSource")).fire();
            }
        });

        explorerButton.setOnAction(event -> openFileChooser());
        explorerButton.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                explorerButton.fire();
            }
        });
    }

    @Nonnull
    @Override
    public RestSource getResource() throws IOException {
        return new RestSource(chosenFullPath.get().getPath());
    }

    @Nonnull
    @Override
    public ObservableBooleanValue inputChosen() {
        return chosenNotNull;
    }

    /**
     * Tries to retrieve the initial directory from the configuration file.
     * If that fails or the dir in the config doesn't exist, the current directory will be used.
     *
     * @return Initial directory
     */
    @Nonnull
    private File getInitialDirectory() {
        File dir = new File(ConfigKey.initialRestDirectory.get(DEFAULT_DIR));
        if (!dir.isDirectory()) {
            log.info("initial REST dir is invalid, falling back to current directory.");
            dir = new File(DEFAULT_DIR);
        }
        return dir;
    }

    /**
     * Tries to retrieve the initial file name of the executable from the configuration file.
     * If that fails, "runRest" will be used.
     *
     * @return initial executable name
     */
    @Nonnull
    private String getInitialExecutable() {
        return ConfigKey.initialRestExecutable.get(DEFAULT_EXECUTABLE);
    }

    /**
     * Opens the system explorer where a file can be chosen. The {@link #chosenDirectory} and {@link
     * #chosenExecutable} properties and the {@link #pathField} text will be updated afterwards.
     *
     * <p>This method will block until the file chooser is closed.</p>
     */
    private void openFileChooser() {
        File chosen = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        setPath(chosen);
        if (chosen != null) {
            pathField.setText(chosenFullPath.get().getPath());
        }
    }

    /**
     * Changes the currently selected path.
     *
     * <p> The path can be null, empty, whitespace, a relative or an absolute path. </p>
     *
     * <p> If the path only contains the executable name (e.g. "runRest"), the working directory is
     * prepended. </p>
     *
     * <p> If the parent directory is invalid, it will be replaced by the working directory. </p>
     *
     * @param path a (manually entered) path to the executable
     */
    private void setPath(@Nullable String path) {
        if (path == null || path.trim().isEmpty()) {
            chosenExecutable.setValue(null);
        } else {
            setPath(new File(path));
        }
    }

    /**
     * Changes the currently selected path.
     *
     * <p> If pathFile only specifies a name (e.g. new File("runRest")), the working directory is
     * assumed as the parent directory and the specified name is used as the executable name (new
     * File(".", pathFile.getName()) is used). </p>
     *
     * <p> If the parent directory is not null, but invalid, it will be replaced by the working
     * directory. </p>
     *
     * @param pathFile the pathFile for the executable
     */
    private void setPath(@Nullable File pathFile) {
        if (pathFile == null) {
            chosenExecutable.setValue(null);
            return;
        }

        String executable = pathFile.getName();
        setPath(pathFile.getParentFile(), executable);
    }

    /**
     * Sets the currently selected path.
     * If the directory is null or invalid, it will be replaced by the working directory.
     *
     * @param directory the directory the executable is in
     * @param executable the file name of the executable
     */
    private void setPath(@Nullable File directory, @Nonnull String executable) {
        if (directory == null || !directory.isDirectory()) {
            directory = new File(DEFAULT_DIR);
        }
        chosenDirectory.setValue(directory);
        chosenExecutable.setValue(executable);
    }
}
