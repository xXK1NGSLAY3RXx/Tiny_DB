package com.csci5408.Utils;

import com.csci5408.Models.Attribute;
import com.csci5408.Models.ColumnType;
import com.csci5408.OS.IO;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ValidColumn {

    public Attribute checkEntireColumn(String name, String type, String size, String isPrimaryKey, String isNotNull, String isUnique, String tableName){

        ColumnType attributeType = checkType(type, tableName);

        if (attributeType == null)
            return null;

        int attributeSize = -1;

        try{
            attributeSize = Integer.parseInt(size);
        }catch (NumberFormatException exception){
            IO.println("Invalid attribute size!");
            return null;
        }

        String attributePrimaryKey = checkPrimaryUniqueNotNullKey(isPrimaryKey, tableName);

        if (attributePrimaryKey == null)
            return null;

        String attributeNotNullKey = checkPrimaryUniqueNotNullKey(isNotNull, tableName);

        if (attributeNotNullKey == null)
            return null;

        String attributeUniqueKey = checkPrimaryUniqueNotNullKey(isUnique, tableName);

        if (attributeUniqueKey == null)
            return null;

        Attribute attribute = new Attribute();
        attribute.setName(name);
        attribute.setType(attributeType);
        attribute.setSize(attributeSize);
        attribute.setPrimaryKey(Boolean.parseBoolean(attributePrimaryKey));
        attribute.setNotNull(Boolean.parseBoolean(attributeNotNullKey));
        attribute.setUnique(Boolean.parseBoolean(attributeUniqueKey));

        return attribute;
    }

    public ColumnType checkType(String type, String tableName){
        if (type.equalsIgnoreCase("INT")) {
            return ColumnType.INT;
        } else if (type.equalsIgnoreCase("FLOAT")) {
            return ColumnType.FLOAT;
        } else if (type.equalsIgnoreCase("DOUBLE")) {
            return ColumnType.DOUBLE;
        } else if (type.equalsIgnoreCase("BOOLEAN")) {
            return ColumnType.BOOLEAN;
        } else if (type.equalsIgnoreCase("VARCHAR")) {
            return ColumnType.VARCHAR;
        } else if (type.equalsIgnoreCase("DATE")) {
            return ColumnType.DATE;
        } else {
            IO.println("Invalid Column Type in table: " + tableName);
            return null;
        }
    }

    public String checkPrimaryUniqueNotNullKey(String isPrimaryUniqueNotNullKey, String tableName){
        if (isPrimaryUniqueNotNullKey.equalsIgnoreCase("false")) {
            return "false";
        } else if (isPrimaryUniqueNotNullKey.equalsIgnoreCase("true")) {
            return "true";
        } else {
            IO.println("Invalid constraint in table: " + tableName);
            return null;
        }
    }


    public boolean validateAndConvert(String value, ColumnType type) {
        switch (type) {
            case INT:
                try {
                    Integer.parseInt(value);
                    return true;
                } catch (NumberFormatException e) {
                    IO.println("Invalid value for INT type: " + value);
                }
            case FLOAT:
                try {
                    Float.parseFloat(value);
                    return true;
                } catch (NumberFormatException e) {
                    IO.println("Invalid value for FLOAT type: " + value);
                }
            case VARCHAR:
                return true;
            case DATE:
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    dateFormat.parse(value);
                    return true;
                } catch (ParseException e) {
                    IO.println("Invalid date format: " + value);
                }
            case BOOLEAN:
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    IO.println("Invalid value for BOOLEAN type: " + value);
                    return false;
                }
                return true;
            case DOUBLE:
                try {
                    Double.parseDouble(value);
                    return true;
                } catch (NumberFormatException e) {
                    IO.println("Invalid value for DOUBLE type: " + value);
                }
            default:
                IO.println("Unsupported data type: " + type);
                return false;
        }
    }

}
