package com.csci5408.Export;

import com.csci5408.OS.FileOperations;
import com.csci5408.OS.IO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DatabaseExporter {

    private FileOperations fileOperations;

    public DatabaseExporter() {
        fileOperations = new FileOperations();
    }

    public void exportDatabase(String dbName) {
        String dbFolder = "DataStore/" + dbName;
        String schemaFile = dbFolder + "/" + dbName + ".txt";

        List<String> dbLines = fileOperations.read("DataStore/metadata.txt");
            if (dbLines == null || !(dbLines.contains(dbName))) {
                System.out.println("Database not found!");
                return;
            }

        List<String> schemaLines = fileOperations.read(schemaFile);
        if (schemaLines == null) {
            IO.println("Failed to read schema file.");
            return;
        }

        StringBuilder sqlDump = new StringBuilder();
        String currentTable = null;
        StringBuilder currentTableSchema = new StringBuilder();

        for (String line : schemaLines) {
            if (line.startsWith("@")) {
                if (currentTable != null) {
                    sqlDump.append(generateCreateTableStatement(currentTable, currentTableSchema.toString()));
                    sqlDump.append(generateInsertStatements(dbFolder, currentTable));
                }
                currentTable = line.substring(1);
                currentTableSchema.setLength(0); // Clear the schema for the next table
            }
            currentTableSchema.append(line).append("\n");
        }

        // Append the last table
        if (currentTable != null) {
            sqlDump.append(generateCreateTableStatement(currentTable, currentTableSchema.toString()));
            sqlDump.append(generateInsertStatements(dbFolder, currentTable));
        }

        writeSqlDumpToFile(dbName, sqlDump.toString());
    }

    private String generateCreateTableStatement(String tableName, String tableSchema) {
        StringBuilder createTable = new StringBuilder("CREATE TABLE " + tableName + " (\n");
        List<String> schemaLines = List.of(tableSchema.split("\n"));
        List<String> foreignKeys = new ArrayList<>();

        for (String line : schemaLines) {
            if (line.startsWith("?")) {
                String[] parts = line.substring(1).split("#");
                String columnName = parts[0];
                String dataType = parts[1].startsWith("VARCHAR") && parts[1].indexOf('(') == -1 ? "VARCHAR(255)" : parts[1];
                boolean isPrimaryKey = parts[3].equals("true");
                boolean isNotNull = parts[4].equals("true");
                boolean isUnique = parts[5].equals("true");

                createTable.append(columnName).append(" ").append(dataType);

                if (isPrimaryKey) {
                    createTable.append(" PRIMARY KEY");
                }

                if (isNotNull) {
                    createTable.append(" NOT NULL");
                }

                if (isUnique && !isPrimaryKey) { // Only append UNIQUE if it's not a primary key
                    createTable.append(" UNIQUE");
                }

                createTable.append(",\n");
            } else if (line.startsWith("&")) {
                String[] parts = line.substring(1).split("#");
                foreignKeys.add("FOREIGN KEY (" + parts[0] + ") REFERENCES " + parts[2] + " (" + parts[1] + ")");
            }
        }

        // Add foreign keys
        for (String fk : foreignKeys) {
            createTable.append(fk).append(",\n");
        }

        createTable.setLength(createTable.length() - 2); // Remove last comma and newline
        createTable.append("\n);\n");
        return createTable.toString();
    }


    private String generateInsertStatements(String dbFolder, String tableName) {
        StringBuilder insertStatements = new StringBuilder();
        String tableFile = dbFolder + "/" + tableName + ".txt";
        List<String> dataLines = fileOperations.read(tableFile);

        if (dataLines != null) {
            for (String row : dataLines) {
                String[] values = row.split("#");
                insertStatements.append("INSERT INTO ").append(tableName).append(" VALUES (");
                for (String value : values) {
                    insertStatements.append("'").append(value).append("', ");
                }
                insertStatements.setLength(insertStatements.length() - 2); // Remove last comma and space
                insertStatements.append(");\n");
            }
        }
        return insertStatements.toString();
    }

    private void writeSqlDumpToFile(String dbName, String sqlDump) {

        if (!fileOperations.isDirectoryFileExist("DataStore/" + dbName + "/Export")){
            fileOperations.createDirectory("DataStore/" + dbName + "/Export");
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyHHmmss");
        String timestamp = now.format(formatter);

        String dumpFile = "DataStore/" + dbName + "/Export/" + dbName + "_" + timestamp + "_dump.sql";
        try (FileWriter fileWriter = new FileWriter(dumpFile)) {
            fileWriter.write(sqlDump);
        } catch (IOException e) {
            IO.println("Failed to write SQL dump: " + e.getMessage());
        }
    }
}
