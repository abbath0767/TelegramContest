package com.ng.telegramcontest.data;

import android.support.annotation.Nullable;

public class DataSet {
    private long[] values;
    private final Type type;
    private final String name;
    private String typeName;
    private String entityName;
    @Nullable
    private String color;

    public DataSet(long[] values, Type type, String name, @Nullable String color) {
        this.values = values;
        this.type = type;
        this.name = name;
        this.color = color;
    }

    public long[] getValues() {
        return values;
    }

    public Type getType() {
        return type;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getColor() {
        return color;
    }

    public void setValues(long[] values) {
        this.values = values;
    }

    public void setColor(@Nullable String color) {
        this.color = color;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    enum Type {
        X, Y
    }

    @Override
    public String toString() {
        return "DataSet: {Type: " + type + ", name: " + name + ", color: " + color + ", size: " + values.length + "}";
    }
}
