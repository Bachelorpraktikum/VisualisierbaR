package com.github.bachelorpraktikum.dbvisualization.config;

import com.sun.javafx.binding.Logging;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ConfigFile extends Properties {
    private final static String USER_HOME = System.getProperty("user.home");
    private static final Logger log = Logger.getLogger(ConfigFile.class.getName());

    private static ConfigFile instance = new ConfigFile();

    public static ConfigFile getInstance() {
        return instance;
    }

    private String filepath;

    private ConfigFile() {
        this(String.format("%s/%s", USER_HOME, "ebd.cfg"));
    }

    private ConfigFile(String filepath) {
        super();

        this.filepath = filepath;
        load();
    }

    public void store() {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filepath);
            store(outputStream);
        } catch (IOException io) {
            log.severe(String.format("Couldn't write to %s due to error: %s.", filepath, io.getMessage()));
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void store(OutputStream outputStream) throws IOException {
        store(outputStream, null);
    }

    public void load() {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filepath);
            load(inputStream);
        } catch (IOException io) {
            log.severe(String.format("Couldn't load %s due to error: %s.", filepath, io.getMessage()));
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public Paint[] getTrainColors() {
        final String defaultColorStrings = "GREEN;ORANGE;BROWN;DARKMAGENTA";
        final Paint[] defaultColors = new Paint[]{Color.GREEN, Color.ORANGE, Color.BROWN, Color.DARKMAGENTA};
        String colorsKey = ResourceBundle.getBundle("config_keys").getString("colorsKey");
        String colorValue = String.valueOf(getOrDefault(colorsKey, defaultColors));
        if (colorValue.isEmpty()) {
            colorValue = defaultColorStrings;
        }
        String[] colorStrings = colorValue.split(";");
        Paint[] colors = new Paint[colorStrings.length];

        for (int i = 0; i < colorStrings.length; i++) {
            try {
                colors[i] = Color.valueOf(colorStrings[i]);
            } catch (IllegalArgumentException ignored) {
                Logger.getLogger(getClass().getName()).warning(String.format("%s is not a supported color.", colorStrings[i]));
            }
        }

        if (colors.length == 0) {
            colors = defaultColors;
        }
        return colors;
    }
}
