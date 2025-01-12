package com.csci5408.Models;

public class Cardinality {

    private String fromTableName;
    private String fromTableColumnName;

    @Override
    public String toString() {
        return "Cardinality{" +
                "fromTableName='" + fromTableName + '\'' +
                ", fromTableColumnName='" + fromTableColumnName + '\'' +
                ", toTableName='" + toTableName + '\'' +
                ", toTableColumnName='" + toTableColumnName + '\'' +
                ", cardinality='" + cardinality + '\'' +
                '}';
    }

    private String toTableName;
    private String toTableColumnName;
    private String cardinality;

    public Cardinality(){}

    public String getFromTableName() {
        return fromTableName;
    }

    public void setFromTableName(String fromTableName) {
        this.fromTableName = fromTableName;
    }

    public String getFromTableColumnName() {
        return fromTableColumnName;
    }

    public void setFromTableColumnName(String fromTableColumnName) {
        this.fromTableColumnName = fromTableColumnName;
    }

    public String getToTableName() {
        return toTableName;
    }

    public void setToTableName(String toTableName) {
        this.toTableName = toTableName;
    }

    public String getToTableColumnName() {
        return toTableColumnName;
    }

    public void setToTableColumnName(String toTableColumnName) {
        this.toTableColumnName = toTableColumnName;
    }

    public String getCardinality() {
        return cardinality;
    }

    public void setCardinality(String cardinality) {
        this.cardinality = cardinality;
    }
}
