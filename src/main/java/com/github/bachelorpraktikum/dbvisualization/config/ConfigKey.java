package com.github.bachelorpraktikum.dbvisualization.config;

public enum ConfigKey {

    initialLogFileDirectory("logFile_initialDirectory"),
    colors("trainColors"),
    speedCheckDelta("speedCheckDelta"),
    initialDatabaseUri("database_initialDirectory");

    private final String key;

    ConfigKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return getKey();
    }
}
