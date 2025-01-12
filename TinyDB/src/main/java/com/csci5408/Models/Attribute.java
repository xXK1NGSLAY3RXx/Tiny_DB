package com.csci5408.Models;

public class Attribute {
    private String name;
    private ColumnType type;
    private int size;
    private boolean primaryKey;
    private boolean notNull;
    private boolean unique;

    public Attribute(){}

    @Override
    public String toString() {
        return "Attribute{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", size=" + size +
                ", primaryKey=" + primaryKey +
                ", notNull=" + notNull +
                ", unique=" + unique +
                '}';
    }

    public Attribute(String name, ColumnType type, int size, boolean primaryKey, boolean notNull, boolean unique) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.primaryKey = primaryKey;
        this.notNull = notNull;
        this.unique = unique;

        // If it's a primary key, it should be unique and not null
        if (primaryKey) {
            this.unique = true;
            this.notNull = true;
        }
    }

    // Getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnType getType() {
        return type;
    }

    public void setType(ColumnType type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
        if (primaryKey) {
            this.unique = true;
            this.notNull = true;
        }
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }
}
