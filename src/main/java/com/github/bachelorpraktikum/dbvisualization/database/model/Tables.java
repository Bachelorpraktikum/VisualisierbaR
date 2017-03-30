package com.github.bachelorpraktikum.dbvisualization.database.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public enum Tables {
    BETRIEBSSTELLEN("betriebsstellen", new String[]{
        "id", "titel", "Kurzname", "rl100", "wetter_ID", "Kennziffer_BST"
    }, null),

    VERTICES("vertices", new String[]{
        "ID", "betriebsstellen_ID", "Kennziffer", "name", "km", "direction", "edge_ID", "XLocal",
        "YLocal", "XGlobal", "YGlobal"
    }, "betriebsstellen_ID IS NOT NULL"),

    OBJECT_OBJECT_ATTRIBUTES("object_object_attributes", new String[]{
        "ID", "object1_ID", "object2_ID", "attribute_ID"
    }, null),

    NEIGHBORS("neighbors", new String[]{
        "ID", "object1_ID", "object2_ID"
    }, null),

    EDGES("edges", new String[]{
        "ID", "vertex_ID_from", "vertex_ID_to", "wayNumber"
    }, null),

    OBJECT_ATTRIBUTES("objects_attributes", new String[]{
        "ID", "type", "object_ID", "attribute_ID", "value"
    }, null),

    ATTRIBUTES("attributes", new String[]{
        "ID", "titel", "description", "acronym"
    }, null);

    private String name;
    private List<String> columnNames;
    private String whereCondition;

    Tables(String name, String[] columnNames, String whereCondition) {
        this.columnNames = new LinkedList<>();
        Collections.addAll(this.columnNames, columnNames);
        this.name = name;
        this.whereCondition = whereCondition;
    }

    public String getName() {
        return name;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public Optional<String> getWhereCondition() {
        return Optional.ofNullable(whereCondition);
    }

    @Override
    public String toString() {
        String formatable = "%s | [%s] | %s";
        return String.format(formatable, getName(), String.join(", ", getColumnNames()),
            getWhereCondition());
    }
}
