package com.github.bachelorpraktikum.dbvisualization.database.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Optional;

public class ObjectAttribute implements Element {

    private final int id;
    private final String type;
    private Vertex object;
    private final int objectID;
    private Attribute attribute;
    private final int attributeID;
    private final String value;

    public ObjectAttribute(int id, String type, Vertex object, Attribute attribute,
        String value) {
        this.id = id;
        this.type = type;
        this.object = object;
        objectID = object.getId();
        this.attribute = attribute;
        attributeID = attribute.getId();
        this.value = value;
    }

    public ObjectAttribute(ResultSet rs) throws SQLException {
        Iterator<String> columnNames = Tables.OBJECT_ATTRIBUTES.getColumnNames().iterator();
        id = rs.getInt(columnNames.next());
        type = rs.getString(columnNames.next());
        objectID = rs.getInt(columnNames.next());
        attributeID = rs.getInt(columnNames.next());
        value = rs.getString(columnNames.next());
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Optional<Vertex> getVertex() {
        return Optional.ofNullable(object);
    }

    public int getVertexID() {
        return objectID;
    }

    public Optional<Attribute> getAttribute() {
        return Optional.ofNullable(attribute);
    }

    public int getAttributeID() {
        return attributeID;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        String formatable = "%d | %s | [%s] | [%s] | %s";
        return String
            .format(formatable, getId(), getType(), getVertex(), getAttribute(), getValue());
    }
}