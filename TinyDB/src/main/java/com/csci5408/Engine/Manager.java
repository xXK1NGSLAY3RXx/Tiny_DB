package com.csci5408.Engine;

import com.csci5408.Models.Attribute;
import com.csci5408.OS.FileOperations;
import com.csci5408.OS.IO;
import com.csci5408.OS.Logger;
import com.csci5408.Utils.CustomException;

import java.util.*;

public class Manager {
    private List<String> databases;
    private String currentDatabase;
    Database database;
    private String DataBase_FILE_PATH = "DataStore";
    private FileOperations fileOperations;
    private Logger logger;
    private TransactionManager transactionManager;

    public Manager() {
        databases = new ArrayList<>();
        database = new Database();
        currentDatabase = "";
        fileOperations = new FileOperations();
        loadDatabases();
        logger = Logger.getInstance();
        transactionManager = TransactionManager.getInstance();;
    }

    @Override
    public String toString() {
        return "Manager{" +
                "databases=" + databases +
                '}';
    }

    public boolean createDatabase(String dbName) {

        if (databases.contains(dbName)) {
            IO.println("Failed to create database '" + dbName + "' as it already exists.");
            logger.logEvent("Failed", "Failed to create database '" + dbName + "' as it already exists.");
            return false;
        }

        List<String> content = new ArrayList<>();
        content.add(dbName);

        boolean updateMetaData = fileOperations.write(DataBase_FILE_PATH  + "/metadata.txt", content, false);
        fileOperations.createDirectory(DataBase_FILE_PATH + "/" + dbName);

        databases.add(dbName);

        return updateMetaData;
    }

    public boolean deleteDatabase(String dbName) {
        if (!databases.contains(dbName)) {
            IO.println("Failed to delete database '" + dbName + "' as it does not exist.");
            logger.logEvent("Failed", "Failed to delete database '" + dbName + "' as it does not exist.");
            return false;
        }

        if (fileOperations.isDirectoryFileExist(DataBase_FILE_PATH + "/" + dbName)) {
            List<String> content = fileOperations.read(DataBase_FILE_PATH  + "/metadata.txt");
            List<String> updatedContent = new ArrayList<>(content);

            for (String currLine : content) {
                if (currLine.equals(dbName)) {
                    updatedContent.remove(dbName);
                }
            }

            boolean updateMetaData = fileOperations.write(DataBase_FILE_PATH  + "/metadata.txt", updatedContent, true);
            fileOperations.deleteDirectoryOrFile(DataBase_FILE_PATH + "/" + dbName);
            databases.remove(dbName);

            return updateMetaData;
        } else {
            IO.println("Failed to delete database '" + dbName + "' as it does not exist.");
            logger.logEvent("Failed", "Failed to delete database '" + dbName + "' as it does not exist.");
        }

        return false;
    }

    public boolean useDatabase(String dbName) {
        if (!databases.contains(dbName)) {
            IO.println("Failed to use database '" + dbName + "' as it does not exist.");
            logger.logEvent("Failed", "Failed to use database '" + dbName + "' as it does not exist.");
            return false;
        }
        transactionManager.setCurrentDBName(dbName);
        currentDatabase = dbName;
        database.setCurrDBName(dbName);
        database.loadTables();

        return true;
    }

    public boolean createTable(String tableName, List<Attribute> attributes) {
        if (currentDatabase == null || currentDatabase.isBlank()) {
            IO.println("Failed to create table '" + tableName + "' as no database is selected.");
            logger.logEvent("Failed", "Failed to create table '" + tableName + "' as no database is selected.");
            return false;
        }

        return database.createTable(tableName, attributes);
    }

    public boolean insertIntoTable(String tableName, List<String> values) throws CustomException {
        if (currentDatabase == null || currentDatabase.isBlank()) {
            IO.println("Failed to insert into table '" + tableName + "' as no database is selected.");
            logger.logEvent("Failed", "Failed to insert into table '" + tableName + "' as no database is selected.");
            return false;
        }

        return database.insertTable(tableName, values);
    }

    public List<Map<String, Object>> selectFromTable(String tableName, List<String> columns, String conditionColumn, String conditionValue) throws CustomException {
        if (currentDatabase == null || currentDatabase.isBlank()) {
            IO.println("Failed to select from table '" + tableName + "' as no database is selected.");
            logger.logEvent("Failed", "Failed to select from table '" + tableName + "' as no database is selected.");
            return null;
        }

        return database.selectTable(tableName, columns, conditionColumn, conditionValue);
    }

    public boolean updateFromTable(String tableName, String column, String value, String conditionColumn, String conditionValue) throws CustomException {
        if (currentDatabase == null || currentDatabase.isBlank()) {
            IO.println("Failed to update table '" + tableName + "' as no database is selected.");
            logger.logEvent("Failed", "Failed to update table '" + tableName + "' as no database is selected.");
            return false;
        }

        return database.updateTable(tableName, column, value, conditionColumn, conditionValue);
    }

    public boolean deleteFromTable(String tableName, String conditionColumn, String conditionValue) throws CustomException {
        if (currentDatabase == null || currentDatabase.isBlank()) {
            IO.println("Failed to delete from table '" + tableName + "' as no database is selected.");
            logger.logEvent("Failed", "Failed to delete from table '" + tableName + "' as no database is selected.");
            return false;
        }

        return database.deleteTable(tableName, conditionColumn, conditionValue);
    }

    public boolean dropTable(String tableName) {
        if (currentDatabase == null || currentDatabase.isBlank()) {
            IO.println("Failed to drop table '" + tableName + "' as no database is selected.");
            logger.logEvent("Failed", "Failed to drop table '" + tableName + "' as no database is selected.");
            return false;
        }

        return database.dropTable(tableName);
    }

    public String getCurrentDatabase() {
        return this.currentDatabase;
    }

    public void loadDatabases() {
        if (databases != null && !databases.isEmpty()) {
            this.databases.clear();
        }

        List<String> content = fileOperations.read("DataStore/metadata.txt");

        if (content != null || !content.isEmpty()) {
            this.databases = content;
        }
    }

    public String getDatabaseState() {
        return database.getDatabaseState();
    }

    public void createCopy(){
        database.createCopy();
    }

    public void revertBack(){
        database.revertBack();
    }
}
