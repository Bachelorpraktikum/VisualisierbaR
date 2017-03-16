package com.github.bachelorpraktikum.dbvisualization.config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum ConfigKey {

    initialLogFileDirectory("logFile_initialDirectory"),
    colors("trainColors"),
    speedCheckDelta("speedCheckDelta"),
    initialDatabaseUri("database_initialDirectory"),
    graphExportDimensions("graph_export_dimensions"),
    chartExportDimensions("chart_export_dimensions"),
    initialRestDirectory("rest_initialDirectory"),
    initialRestExecutable("rest_initialExecutable"),
    initialDatabasePort("database_initialPort"),
    initialDatabaseName("database_initialName");

    private final String key;

    ConfigKey(String key) {
        this.key = key;
    }

    @Nonnull
    public String getKey() {
        return key;
    }

    @Nullable
    public String get() {
        return ConfigFile.getInstance().getProperty(getKey());
    }

    @Nonnull
    public String get(@Nonnull String defaultValue) {
        return ConfigFile.getInstance().getProperty(getKey(), defaultValue);
    }

    public void set(@Nullable String value) {
        if (value == null) {
            ConfigFile.getInstance().remove(getKey());
        } else {
            ConfigFile.getInstance().setProperty(getKey(), value);
        }
    }

    @Override
    public String toString() {
        return getKey();
    }
}
