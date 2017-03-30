package com.github.bachelorpraktikum.visualisierbar;

import com.github.bachelorpraktikum.visualisierbar.config.ConfigFile;
import com.github.bachelorpraktikum.visualisierbar.datasource.DataSource;
import com.github.bachelorpraktikum.visualisierbar.datasource.InputParserSource;
import com.github.bachelorpraktikum.visualisierbar.datasource.RestSource;
import com.github.bachelorpraktikum.visualisierbar.view.DataSourceHolder;
import com.github.bachelorpraktikum.visualisierbar.view.MainController;
import com.github.bachelorpraktikum.visualisierbar.view.sourcechooser.SourceController;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class Visualisierbar extends Application {

    private static final String APP_NAME_KEY = "app_name";
    private static Alert licenceInfo;

    static {
        try (InputStream in = Visualisierbar.class.getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(in);
        } catch (IOException e) {
            System.out.println("Can't initialize logging!");
            System.exit(100);
        }
    }

    private static final Logger log = Logger.getLogger(Visualisierbar.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
        licenceInfo = new Alert(Alert.AlertType.INFORMATION);
        licenceInfo.setTitle("Licence Information");
        licenceInfo.setHeaderText("VisualisierbaR");
        licenceInfo.setContentText("MIT License\n"
            + "\n"
            + "Copyright (c) 2016 Torben Carstens, Björn Petersen, Yannick Roder, Christian Schaarschmidt, Johannes Semsch\n"
            + "\n"
            + "Permission is hereby granted, free of charge, to any person obtaining a copy\n"
            + "of this software and associated documentation files (the \"Software\"), to deal\n"
            + "in the Software without restriction, including without limitation the rights\n"
            + "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n"
            + "copies of the Software, and to permit persons to whom the Software is\n"
            + "furnished to do so, subject to the following conditions:\n"
            + "\n"
            + "The above copyright notice and this permission notice shall be included in all\n"
            + "copies or substantial portions of the Software.\n"
            + "\n"
            + "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n"
            + "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n"
            + "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n"
            + "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n"
            + "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n"
            + "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n"
            + "SOFTWARE.\n");
        licenceInfo.getDialogPane().setPrefWidth(525);
        licenceInfo.getDialogPane().setMaxWidth(Double.MAX_VALUE);

        ResourceBundle localizationBundle = ResourceBundle.getBundle("bundles.localization");
        primaryStage.setTitle(localizationBundle.getString(APP_NAME_KEY));
        primaryStage.setOnHiding(event ->
            DataSourceHolder.getInstance().ifPresent(dataSource -> {
                try {
                    dataSource.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            })
        );

        FXMLLoader loader = new FXMLLoader();
        loader.setResources(localizationBundle);

        if (getParameters().getUnnamed().contains("--live")
            || getParameters().getUnnamed().contains("--pipe")) {
            log.info("Loading...");

            DataSource dataSource;
            // start the data source
            if (getParameters().getUnnamed().contains("--live")) {
                dataSource = new RestSource();
            } else {
                dataSource = new InputParserSource(System.in);
            }

            log.info("Launching GUI...");
            // skip the chooser window
            loader.setLocation(
                getClass().getResource("view/MainView.fxml")
            );
            loader.load();
            MainController controller = loader.getController();
            controller.setDataSource(dataSource);
            controller.setStage(primaryStage);
        } else {
            loader.setLocation(
                getClass().getResource("view/sourcechooser/SourceChooser.fxml")
            );

            loader.load();
            SourceController controller = loader.getController();
            controller.setStage(primaryStage);
        }

        primaryStage.show();
    }

    /**
     * Shows a dialog and blocks until it is closed.
     */
    public static void showLicenceInfo() {
        licenceInfo.showAndWait();
    }

    public static void main(String[] args) {
        Application.launch(Visualisierbar.class, args);
        ConfigFile.getInstance().store();
    }
}
