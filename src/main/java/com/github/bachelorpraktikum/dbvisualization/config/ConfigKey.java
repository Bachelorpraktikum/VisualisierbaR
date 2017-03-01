package com.github.bachelorpraktikum.dbvisualization.config;

public enum ConfigKey {

    initialDirectory("%s_initialDirectory"),
    colors("trainColors");

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
