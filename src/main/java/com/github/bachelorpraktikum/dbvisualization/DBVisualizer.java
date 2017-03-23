package com.github.bachelorpraktikum.dbvisualization;

import com.github.bachelorpraktikum.dbvisualization.config.ConfigFile;
import com.github.bachelorpraktikum.dbvisualization.datasource.DataSource;
import com.github.bachelorpraktikum.dbvisualization.datasource.InputParserSource;
import com.github.bachelorpraktikum.dbvisualization.datasource.RestSource;
import com.github.bachelorpraktikum.dbvisualization.view.DataSourceHolder;
import com.github.bachelorpraktikum.dbvisualization.view.MainController;
import com.github.bachelorpraktikum.dbvisualization.view.sourcechooser.SourceController;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class DBVisualizer extends Application {

    private static final String APP_NAME_KEY = "app_name";

    static {
        try (InputStream in = DBVisualizer.class.getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(in);
        } catch (IOException e) {
            System.out.println("Can't initialize logging!");
            System.exit(100);
        }
    }

    private static final Logger log = Logger.getLogger(DBVisualizer.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
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

    public static void main(String[] args) {
        Application.launch(DBVisualizer.class, args);
        ConfigFile.getInstance().store();
    }
}
