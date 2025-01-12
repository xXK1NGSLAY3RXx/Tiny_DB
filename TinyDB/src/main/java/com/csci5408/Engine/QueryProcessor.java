package com.csci5408.Engine;

import com.csci5408.Models.Attribute;
import com.csci5408.Models.ColumnType;
import com.csci5408.OS.IO;
import com.csci5408.OS.Logger;
import com.csci5408.Utils.CustomException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class QueryProcessor {
    private Manager manager;
    private Table table;
    private Logger logger;
    private TransactionManager transactionManager;

    public QueryProcessor(Manager manager) {
        this.manager = manager;
        this.table = new Table();
        this.logger = Logger.getInstance();
        this.transactionManager = TransactionManager.getInstance();
    }

    public void processQueries(List<String> queries) throws CustomException {
        for (String query : queries) {
            long startTime = System.currentTimeMillis();
            boolean success = false;
            try {
                success = processQuery(query.trim().replaceAll(";$", ""));
                if (success) {
                    long endTime = System.currentTimeMillis();
                    long executionTime = endTime - startTime;
                    if (!query.toUpperCase().startsWith("USE") && !query.toUpperCase().contains("CREATE DATABASE")) {
                        logger.logGeneral("Successful", query + " executed in " + executionTime + " ms. " + manager.getDatabaseState());
                    }
                }
            } catch (CustomException e) {
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                logger.logEvent("Failed", "Error processing query '" + query + " ms: " + e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean processQuery(String query) throws IOException, CustomException {
        String[] parts = query.trim().split("\\s+");
        String command = parts[0].toUpperCase();

        switch (command) {
            case "CREATE":
                return processCreateQuery(parts, query);
            case "USE":
                return processUseQuery(parts);
            case "INSERT":
                return processInsertQuery(parts);
            case "SELECT":
                return processSelectQuery(query);
            case "UPDATE":
                return processUpdateQuery(query);
            case "DELETE":
                return processDeleteQuery(query);
            case "DROP":
                return processDropQuery(parts);
            case "START":
                if (parts.length > 1 && parts[1].equalsIgnoreCase("TRANSACTION")) {
                    transactionManager.startTransaction();
                    manager.createCopy();

                    return true;
                }
                throw new CustomException("Unsupported SQL command: " + query);
            case "COMMIT":
                transactionManager.commitTransaction();
                return true;
            case "ROLLBACK":
                transactionManager.rollbackTransaction();
                manager.revertBack();
                return true;
            default:
                IO.println("Unsupported SQL command");
                throw new CustomException("Unsupported SQL command");

        }
    }

    private boolean processCreateQuery(String[] parts, String query) throws CustomException {
        if (parts.length < 3) {
            System.out.println(parts[1]);
            IO.println("Invalid CREATE query format.");
            throw new CustomException("Invalid CREATE query format.");
        }
        String objectType = parts[1].toUpperCase();

        switch (objectType) {
            case "DATABASE":
                return processCreateDatabase(parts);
            case "TABLE":
                return processCreateTable(query);
            default:
                IO.println("Unsupported CREATE command");
                throw new CustomException("Unsupported CREATE command");

        }
    }

    private boolean processCreateDatabase(String[] parts) throws CustomException {
        if (parts.length != 3 || !parts[1].equalsIgnoreCase("DATABASE")) {
            IO.println("Invalid CREATE DATABASE query format.");
            throw new CustomException("Invalid CREATE DATABASE query format.");
        }
        String dbName = parts[2];
        boolean success = manager.createDatabase(dbName);
        if (success) {
            logger.logEvent("Successful", "Database '" + dbName + "' created successfully.");
        }
        return success;
    }

    private boolean processCreateTable(String query) throws CustomException {

        String[] parts = query.trim().split("\\s+", 4);

        if (parts.length < 4 || !parts[1].equalsIgnoreCase("TABLE")) {
            throw new CustomException("Invalid CREATE TABLE query format.");
        }

        String tableName = parts[2];
        List<Attribute> attributes = new ArrayList<>();

        String attributeString = query.substring(query.indexOf("(") + 1, query.lastIndexOf(")"));
        String[] attributeParts = attributeString.split(",");

        for (String attr : attributeParts) {
            String[] attrDetails = attr.trim().split("\\s+");
            String attributeName = attrDetails[0];
            ColumnType attributeType;
            try {
                attributeType = ColumnType.valueOf(attrDetails[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                IO.println("Invalid attribute type '" + attrDetails[1] + "' for attribute '" + attributeName + "' in table creation.");
                throw new CustomException("Invalid attribute type '" + attrDetails[1] + "' for attribute '" + attributeName + "' in table creation.");
            }
            int attributeSize = 0; // Default size, adjust as needed

            Attribute attribute = new Attribute(attributeName, attributeType, attributeSize, false, false, false);

            for (int i = 2; i < attrDetails.length; i++) {
                String constraint = attrDetails[i].toUpperCase();

                if (i == 2) {
                    try {
                        Integer.parseInt(constraint);
                        attribute.setSize(Integer.parseInt(constraint));
                        continue;
                    } catch (NumberFormatException e) {
                        // Ignore and continue processing constraints
                    }
                }

                if (constraint.equals("PRIMARY") && attrDetails[i + 1].equalsIgnoreCase("KEY")) {
                    attribute.setPrimaryKey(true);
                    attribute.setNotNull(true);
                    attribute.setUnique(true);
                    i++; // Skip the next part as it's "KEY"
                } else if (constraint.equals("NOT") && attrDetails[i + 1].equalsIgnoreCase("NULL")) {
                    attribute.setNotNull(true);
                    i++; // Skip the next part as it's "NULL"
                } else if (constraint.equals("UNIQUE")) {
                    attribute.setUnique(true);
                    attribute.setNotNull(true);
                }
            }

            attributes.add(attribute);
        }

        if (attributes.isEmpty()) {
            IO.println("No attributes found for table creation.");
            throw new CustomException("No attributes found for table creation.");
        }

        return manager.createTable(tableName, attributes);
    }

    private boolean processUseQuery(String[] parts) throws CustomException {
        if (parts.length != 2) {
            IO.println("Invalid USE DATABASE query format.");
            throw new CustomException("Invalid USE DATABASE query format.");
        }
        String dbName = parts[1];
        boolean success = manager.useDatabase(dbName);
        if (success) {
            logger.logEvent("Successful", "Using database '" + dbName + "'.");
        }
        return success;
    }

    private boolean processInsertQuery(String[] parts) throws CustomException {
        if (parts.length != 5 || !"INSERT".equalsIgnoreCase(parts[0]) || !"INTO".equalsIgnoreCase(parts[1])) {
            IO.println("Invalid INSERT query format.");
            throw new CustomException("Invalid INSERT query format.");

        }

        String tableName = parts[2];

        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();

        for (String currPart : parts) {
            if (currPart.equalsIgnoreCase("VALUES")) {
                int columnsStartIndex = parts[4].indexOf("(");
                int columnsEndIndex = parts[4].indexOf(")");
                int valuesStartIndex = parts[4].lastIndexOf("(");
                int valuesEndIndex = parts[4].lastIndexOf(")");

                if (columnsStartIndex == valuesStartIndex) {
                    String valuesPart = parts[4].substring(valuesStartIndex + 1, valuesEndIndex).trim();
                    values = Arrays.asList(valuesPart.split(","));
                } else {
                    String columnsPart = parts[4].substring(columnsStartIndex + 1, columnsEndIndex).trim();
                    String valuesPart = parts[4].substring(valuesStartIndex + 1, valuesEndIndex).trim();

                    columns = Arrays.asList(columnsPart.split(","));
                    values = Arrays.asList(valuesPart.split(","));
                }
            }
        }

        for (int i = 0; i < values.size(); i++) {
            values.set(i, values.get(i).trim().replaceAll("^'(.*)'$", "$1"));
        }

        return manager.insertIntoTable(tableName, values);
    }

    private boolean processSelectQuery(String query) throws CustomException {
        query = query.trim().replaceAll(";$", "");

        if (query.contains("select")){
            query = query.replace("select", "SELECT");
        }

        if (query.contains("from")){
            query= query.replace("from", "FROM");
        }

        if (query.contains("where")){
            query= query.replace("where", "WHERE");
        }


        String[] parts = query.split("\\s+FROM\\s+", 2);

        if (parts.length != 2 || parts[0].trim().equalsIgnoreCase("SELECT")) {
            IO.println("Invalid SELECT query format. Column names are missing.");
            throw new CustomException("Invalid SELECT query format. Column names are missing.");
        }

        String selectPart = parts[0].substring("SELECT".length()).trim();

        String fromPart = parts[1];

        String columnsPart = selectPart.replaceAll("\\s*,\\s*", ",");
        List<String> columns;
        if (columnsPart.equals("*")) {
            columns = null;
        } else {
            columns = Arrays.asList(columnsPart.split(","));
        }

        String tableName;
        String conditionColumn = null;
        String conditionValue = null;

        if (fromPart.toUpperCase().contains("WHERE")) {
            String[] tableAndConditionParts = fromPart.split("\\s+WHERE\\s+", 2);
            tableName = tableAndConditionParts[0].trim();
            String[] conditionParts = tableAndConditionParts[1].split("=", 2);
            if (conditionParts.length != 2) {
                IO.println("Invalid WHERE clause format.");
                throw new CustomException("Invalid WHERE clause format.");
            }
            conditionColumn = conditionParts[0].trim();
            conditionValue = conditionParts[1].trim().replaceAll("^'(.*)'$", "$1");
        } else {
            tableName = fromPart.trim();
        }

        List<Map<String, Object>> result = manager.selectFromTable(tableName, columns, conditionColumn, conditionValue);

        if (result == null || result.isEmpty()) {
            return false;
        }

        // Process the result, e.g., print it
        IO.println("Query result:");
        if (columns == null) {
            // Fetch all columns if * was specified
            table.setCurrDBName(manager.getCurrentDatabase());
            table.SetAttribute(tableName);
            columns = table.getColumnNames();
        }
        for (Map<String, Object> row : result) {
            for (String col : columns) {
                IO.print(col + ": " + row.get(col) + "\t");
            }
            IO.println("");
        }
        return true;
    }

    private boolean processUpdateQuery(String query) throws CustomException {
        query = query.trim().replaceAll(";$", "");

        if (query.contains("update")){
            query = query.replace("update", "UPDATE");
        }

        if (query.contains("set")){
            query= query.replace("set", "SET");
        }

        if (query.contains("where")){
            query= query.replace("where", "WHERE");
        }

        int setIndex = query.toUpperCase().indexOf("SET");
        int whereIndex = query.toUpperCase().indexOf("WHERE");

        if (setIndex == -1 || whereIndex == -1 || setIndex >= whereIndex) {
            IO.println("Invalid UPDATE query format.");
            throw new CustomException("Invalid UPDATE query format.");
        }

        String tableName = query.substring(6, setIndex).trim();

        String setPart = query.substring(setIndex + 3, whereIndex).trim();
        String[] setParts = setPart.split("=", 2);
        if (setParts.length != 2) {
            IO.println("Invalid SET clause format.");
            throw new CustomException("Invalid SET clause format.");
        }
        String column = setParts[0].trim();
        String value = setParts[1].trim().replaceAll("^'(.*)'$", "$1");

        String wherePart = query.substring(whereIndex + 5).trim();
        String[] whereParts = wherePart.split("=", 2);
        if (whereParts.length != 2) {
            IO.println("Invalid WHERE clause format.");
            throw new CustomException("Invalid WHERE clause format.");

        }
        String conditionColumn = whereParts[0].trim();
        String conditionValue = whereParts[1].trim().replaceAll("^'(.*)'$", "$1");

        return manager.updateFromTable(tableName, column, value, conditionColumn, conditionValue);
    }

    private boolean processDeleteQuery(String query) throws CustomException {
        String[] parts = query.trim().split("\\s+");

        if (query.contains("delete")){
            query = query.replace("delete", "DELETE");
        }

        if (query.contains("from")){
            query= query.replace("from", "FROM");
        }

        if (query.contains("where")){
            query= query.replace("where", "WHERE");
        }

        if (parts.length < 3 || !"DELETE".equalsIgnoreCase(parts[0]) || !"FROM".equalsIgnoreCase(parts[1])) {
            IO.println("Invalid DELETE query format.");
            throw new CustomException("Invalid DELETE query format.");
        }

        String tableName = parts[2];

        String conditionColumn = null;
        String conditionValue = null;

        if (query.contains("WHERE")) {
            String wherePart = query.substring(query.indexOf("WHERE") + 5).trim();
            String[] whereParts = wherePart.split("=");
            if (whereParts.length != 2) {
                IO.println("Invalid WHERE clause format.");
                throw new CustomException("Invalid WHERE clause format.");
            }
            conditionColumn = whereParts[0].trim();
            conditionValue = whereParts[1].trim();
        }

        return manager.deleteFromTable(tableName, conditionColumn, conditionValue);
    }


    private boolean processDropQuery(String[] parts) throws CustomException {

        if (parts.length != 3 || !"DROP".equalsIgnoreCase(parts[0]) || !"TABLE".equalsIgnoreCase(parts[1])) {
            IO.println("Invalid DROP TABLE query format.");
            throw new CustomException("Invalid DROP TABLE query format.");
        }

        String tableName = parts[2];
        return manager.dropTable(tableName);
    }
}
