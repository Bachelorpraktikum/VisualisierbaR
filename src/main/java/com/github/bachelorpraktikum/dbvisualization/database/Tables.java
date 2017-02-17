package com.github.bachelorpraktikum.dbvisualization.database;

public enum Tables {
    BETRIEBSSTELLEN("betriebsstellen"),
    VERTICES("vertices"),
    OBJECT_OBJECT_ATTRIBUTES("object_object_attributes"),
    NEIGHBORS("neighbors"),
    EDGES("edges"),
    OBJECTS_ATTRIBUTES("objects_attributes"),
    ATTRIBUTES("attributes");


    private String name;

    Tables(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }
}
