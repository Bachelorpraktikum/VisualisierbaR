package com.github.bachelorpraktikum.dbvisualization.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class ConfigFile extends Properties {
    private static ConfigFile instance = new ConfigFile();

    public static ConfigFile getInstance() {
        return instance;
    }

    private static final Logger log = Logger.getLogger(ConfigFile.class.getName());
    private final static String USER_HOME = System.getProperty("user.home");
    private String filepath;

    private ConfigFile() {
        this(String.format("{}/{}", USER_HOME, "ebd.cfg"));
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
        } catch (IOException io) {
            log.severe(String.format("Couldn't write to {} due to error: {}.", filepath, io.getMessage()));
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

    public void load() {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filepath);
            load(inputStream);
        } catch (IOException io) {
            log.severe(String.format("Couldn't load {} due to error: {}.", filepath, io.getMessage()));
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
}
