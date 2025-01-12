package com.csci5408.Engine;

import com.csci5408.Models.Attribute;
import com.csci5408.OS.FileOperations;
import com.csci5408.OS.IO;
import com.csci5408.Utils.CustomException;
import com.csci5408.Utils.ValidColumn;

import java.util.*;

public class Table {
    private List<Attribute> attributes;
    private FileOperations fileOperations;
    private String currDBName;
    private String DataBase_FILE_PATH = "DataStore";
    private List<List<String>> rows;
    private ValidColumn validColumn;
    private TransactionManager transactionManager;

    public Table() {
        attributes = new ArrayList<>();
        fileOperations = new FileOperations();
        currDBName = "";
        rows = new ArrayList<>();
        validColumn = new ValidColumn();
        this.transactionManager = TransactionManager.getInstance();
    }

    public String getCurrDBName() {
        return currDBName;
    }

    public void setCurrDBName(String currDBName) {
        this.currDBName = currDBName;
    }

    public boolean SetAttribute(String tableName) throws CustomException {
        if (currDBName == null || currDBName.isBlank()) {
            IO.println("Database name is not set.");
            throw new CustomException("Database name is not set.");

        }

        attributes.clear();

        List<String> content = fileOperations.read(transactionManager.getDatabasePath() + "/" + currDBName + ".txt");

        for (int i = 0; i < content.size(); i++) {
            String currLine = content.get(i);
            if (currLine.startsWith("@")) {
                if (currLine.substring(1).equals(tableName)) {
                    i++;

                    while (i < content.size() && content.get(i).startsWith("?")) {

                        String[] column = content.get(i).substring(1).split("#");

                        Attribute attribute = validColumn.checkEntireColumn(column[0], column[1], column[2], column[3], column[4], column[5], tableName);

                        if (attribute == null){
                            return false;
                        }

                        attributes.add(attribute);

                        i++;
                    }
                }
            }
        }

        return true;
    }

    public List<String> getColumnNames() {
        List<String> columnNames = new ArrayList<>();
        for (Attribute attribute : attributes) {
            columnNames.add(attribute.getName());
        }
        return columnNames;
    }

    private void loadTableData(String tableName, String dbName) {
        String tableFilePath = transactionManager.getDatabasePath() + "/" + tableName + ".txt";
        List<String> fileContent = fileOperations.read(tableFilePath);

        rows.clear();
        for (String line : fileContent) {
            if (line.startsWith("@")) {
                continue;
            }
            String[] rowValues = line.split("#");
            List<String> row = new ArrayList<>();
            for (String value : rowValues) {
                row.add(value);
            }
            rows.add(row);
        }
    }

    public boolean insert(String tableName, List<String> values) throws CustomException {
        if (!SetAttribute(tableName)) {
            IO.println("Failed to set attributes for table: " + tableName);
            throw new CustomException("Failed to set attributes for table: " + tableName);

        }

        if (values.size() != attributes.size()) {
            IO.println("Number of values must match number of attributes.");
            throw new CustomException("Number of values must match number of attributes.");
        }

        List<Integer> positions = new ArrayList<>();
        List<String> uniqueValues = new ArrayList<>();

        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            Attribute attribute = attributes.get(i);

            if (!validColumn.validateAndConvert(value, attribute.getType())) {
                IO.println("Validation failed for attribute: " + attribute.getName());
                throw new CustomException("Validation failed for attribute: " + attribute.getName());

            }

            if (attribute.isPrimaryKey() || attribute.isUnique()) {
                positions.add(i);
                uniqueValues.add(value);
            }
        }

        if (!handleUniqueConstraint(tableName, positions, uniqueValues)) {
            IO.println("Unique constraint is violated!");
            throw new CustomException("Unique constraint is violated!");
        }

        List<String> content = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i + 1 < values.size()) {
                stringBuilder.append(values.get(i)).append("#");
            } else {
                stringBuilder.append(values.get(i));
            }
        }
        content.add(stringBuilder.toString());

        return fileOperations.write(transactionManager.getDatabasePath() + "/" + tableName + ".txt", content, false);
    }

    private boolean handleUniqueConstraint(String tableName, List<Integer> positions, List<String> uniqueValues) {

        List<String> content = fileOperations.read(transactionManager.getDatabasePath() + "/" + tableName + ".txt");

        for (String currLine : content) {
            String[] data = currLine.split("#");

            for (int i = 0; i < positions.size(); i++) {
                if (data[positions.get(i)].equalsIgnoreCase(uniqueValues.get(positions.get(i)))) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean areColumnsValid(List<String> columns) throws CustomException {
        for (String column : columns) {
            if (findColumnIndex(column) == -1) {
                IO.println("One or more specified columns do not exist.");
                throw new CustomException("One or more specified columns do not exist.");
            }
        }
        return true;
    }

    public List<Map<String, Object>> select(String tableName, List<String> columns, String conditionColumn, String conditionValue, String dbName) throws CustomException {
        List<Map<String, Object>> results = new ArrayList<>();
        SetAttribute(tableName);
        loadTableData(tableName, dbName);

        if (columns == null) {
            columns = new ArrayList<>();
            for (Attribute attribute : attributes) {
                columns.add(attribute.getName());
            }
        }

        areColumnsValid(columns);

        if (conditionColumn != null && findColumnIndex(conditionColumn) == -1) {
            IO.println("Condition column does not exist.");
            throw new CustomException("Condition column does not exist.");
        }

        for (List<String> row : rows) {
            if (conditionColumn == null || row.get(findColumnIndex(conditionColumn)).equals(conditionValue)) {
                Map<String, Object> resultRow = new HashMap<>();
                for (String column : columns) {
                    int index = findColumnIndex(column);
                    if (index != -1) {
                        if (!validColumn.validateAndConvert(row.get(index), attributes.get(index).getType())){
                            return null;
                        }
                        resultRow.put(attributes.get(index).getName(), row.get(index));
                    }
                }
                results.add(resultRow);
            }
        }

        return results;
    }

    public boolean update(String tableName, String column, String value, String conditionColumn, String conditionValue, String dbName) throws CustomException {
        setCurrDBName(dbName);
        SetAttribute(tableName);
        loadTableData(tableName, dbName);

        int columnIndex = findColumnIndex(column);

        if (columnIndex == -1) {
            IO.println("Column '" + column + "' does not exist.");
            throw new CustomException("Column '" + column + "' does not exist.");
        }

        boolean updated = false;

        for (List<String> row : rows) {
            if (conditionColumn == null || row.get(findColumnIndex(conditionColumn)).equals(conditionValue)) {

                validColumn.validateAndConvert(value, attributes.get(columnIndex).getType());

                if (attributes.get(columnIndex).isUnique()) {
                    for (List<String> otherRow : rows) {
                        if (!otherRow.equals(row) && otherRow.get(columnIndex).equals(value)) {
                            IO.println("Unique constraint violated for attribute '" + attributes.get(columnIndex).getName() + "' with value '" + value + "'.");
                            throw new CustomException("Unique constraint violated for attribute '" + attributes.get(columnIndex).getName() + "' with value '" + value + "'.");
                        }
                    }
                }

                row.set(columnIndex, value);
                updated = true;
            }
        }

        if (updated) {
            saveTableData(tableName, dbName);
            return true;
        } else {
            IO.println("No rows matched the condition.");
            throw new CustomException("No rows matched the condition.");

        }
    }

    private void saveTableData(String tableName, String dbName) {
        String tableFilePath = transactionManager.getDatabasePath() + "/" + tableName + ".txt";
        List<String> fileContent = new ArrayList<>();

        for (List<String> row : rows) {
            fileContent.add(String.join("#", row));
        }

        fileOperations.write(tableFilePath, fileContent, true);
    }

    public boolean delete(String tableName, String conditionColumn, String conditionValue, String dbName) throws CustomException {
        setCurrDBName(dbName);
        SetAttribute(tableName);
        loadTableData(tableName, dbName);

        if (conditionColumn != null && findColumnIndex(conditionColumn) == -1) {
            IO.println("Condition column '" + conditionColumn + "' does not exist.");
            throw new CustomException("Condition column '" + conditionColumn + "' does not exist.");
        }

        boolean deleted = false;

        if (conditionColumn == null) {
            // No condition specified, delete all rows
            rows.clear();
            deleted = true;
        } else {
            for (Iterator<List<String>> iterator = rows.iterator(); iterator.hasNext(); ) {
                List<String> row = iterator.next();
                if (row.get(findColumnIndex(conditionColumn)).equals(conditionValue)) {
                    iterator.remove();
                    deleted = true;
                }
            }
        }

        if (deleted) {
            saveTableData(tableName, dbName);
            return true;
        } else {
            IO.println("No rows matched the condition.");
            throw new CustomException("No rows matched the condition.");
        }
    }


    private int findColumnIndex(String columnName) {
        for (int i = 0; i < attributes.size(); i++) {
            if (attributes.get(i).getName().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    public List<Attribute> getTable(String tableName) {
        return this.attributes;
    }

    public int getRecordCount(String tableName, String dbName) {
        loadTableData(tableName, dbName);
        return rows.size();
    }
}
