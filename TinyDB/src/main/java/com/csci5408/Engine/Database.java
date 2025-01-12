package com.csci5408.Engine;

import com.csci5408.Models.Attribute;
import com.csci5408.Models.Cardinality;
import com.csci5408.OS.FileOperations;
import com.csci5408.OS.IO;
import com.csci5408.OS.Logger;
import com.csci5408.Utils.CustomException;
import com.csci5408.Utils.ValidColumn;

import java.util.*;

public class Database {
    private String currDBName;
    private List<String> tables;
    private List<String> originalTable;
    private Table table;
    private FileOperations fileOperations;
    private Logger logger;
    private String DataBase_FILE_PATH = "DataStore";
    private ValidColumn validColumn;
    private ValidateForeignKey validateForeignKey;
    private TransactionManager transactionManager;

    public Database() {
        this.currDBName = "";
        this.tables = new ArrayList<>();
        table = new Table();
        fileOperations = new FileOperations();
        logger = Logger.getInstance();
        validColumn = new ValidColumn();
        validateForeignKey = new ValidateForeignKey();
        this.transactionManager = TransactionManager.getInstance();
    }

    public void loadTables(){

        this.tables.clear();

        List<String> content = fileOperations.read(transactionManager.getDatabasePath() + "/" + currDBName + ".txt");

        for (String currLine : content){
            if (currLine.startsWith("@")){
                this.tables.add(currLine.substring(1).trim());
            }
        }
    }

    public boolean createTable(String tableName, List<Attribute> attributes) {

        if (tables.contains(tableName)) {
            logger.logEvent("Failed", "Failed to create table '" + tableName + "' as it already exists in database '" + currDBName + "'.");
            IO.println("Table '" + tableName + "' already exists in database '" + currDBName + "'.");
            return false;
        }

        validateForeignKey.setCurrDBName(currDBName);
        List<String> foreignKeys = validateForeignKey.getForeignKeyContent(tableName, attributes);

        if (validateForeignKey == null){
            foreignKeys = new ArrayList<>();
        }

        fileOperations.write(transactionManager.getDatabasePath() + "/" + currDBName + ".txt", prepareDataToCreateTable(tableName, attributes, foreignKeys), false);

        tables.add(tableName);

        return true;
    }

    private List<String> prepareDataToCreateTable(String tableName, List<Attribute> attributes, List<String> foreignKey){

        List<String> content = new ArrayList<>();

        content.add("@" + tableName);

        for (Attribute attribute : attributes){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("?" + attribute.getName() + "#");
            stringBuilder.append(attribute.getType() + "#");
            stringBuilder.append(attribute.getSize() + "#");
            stringBuilder.append(attribute.isPrimaryKey() + "#");
            stringBuilder.append(attribute.isNotNull() + "#");
            stringBuilder.append(attribute.isUnique());

            content.add(stringBuilder.toString());
        }

        for (String string : foreignKey){
            content.add(string);
        }

        return content;
    }

    public List<Attribute> getTable(String tableName) {

        if (!tables.contains(tableName)) {
            logger.logEvent("Failed", "Table '" + tableName + "' doesn't exist in database '" + currDBName + "'.");
            IO.println("Table '" + tableName + "' doesn't exists in database '" + currDBName + "'.");
            return null;
        }
        return table.getTable(tableName);
    }

    public boolean dropTable(String tableName) {

        if (!tables.contains(tableName)) {
            logger.logEvent("Failed", "Table '" + tableName + "' doesn't exist in database '" + currDBName + "'.");
            IO.println("Table '" + tableName + "' doesn't exists in database '" + currDBName + "'.");
            return false;
        }

        if (fileOperations.isDirectoryFileExist(transactionManager.getDatabasePath() + "/" + tableName + ".txt")){

            List<String> content = fileOperations.read(transactionManager.getDatabasePath() + "/" + currDBName + ".txt");

            List<Integer> deleteLineNumbers = new ArrayList<>();

            boolean isLinesDeleted = false;

            for (int i=0;i<content.size();i++){
                String currLine = content.get(i);
                if (currLine.startsWith("@")){
                    if (currLine.substring(1).equals(tableName)){
                        isLinesDeleted = true;
                        deleteLineNumbers.add(i);
                        i++;
                        while (i < content.size() && (content.get(i).startsWith("?") || content.get(i).startsWith("&"))){
                            deleteLineNumbers.add(i);
                            i++;
                        }
                    }
                }

                if (isLinesDeleted){
                    break;
                }
            }

            deleteLineNumbers.sort(Collections.reverseOrder());

            for (int position : deleteLineNumbers) {
                if (position >= 0 && position < content.size()) {
                    content.remove(position);
                }
            }

            boolean updateDatabaseFile = fileOperations.write(transactionManager.getDatabasePath() + "/" + currDBName + ".txt", content, true);
            fileOperations.deleteDirectoryOrFile(transactionManager.getDatabasePath() + "/" + tableName + ".txt");

            tables.remove(tableName);

            return updateDatabaseFile;
        }
        else {
            logger.logEvent("Failed", "Table '" + tableName + "' doesn't exist in database '" + currDBName + "'.");
            IO.println("Table " + tableName + " Doesn't Exist!");
            return false;
        }
    }

    public boolean insertTable(String tableName, List<String> values) throws CustomException {
        if (!tables.contains(tableName)) {
            logger.logEvent("Failed", "Table '" + tableName + "' doesn't exist in database '" + currDBName + "'.");
            IO.println("Table '" + tableName + "' doesn't exist in database '" + currDBName + "'.");
            return false;
        }

        this.table.setCurrDBName(currDBName);
        boolean success = table.insert(tableName, values);
        if (!success) {
            logger.logEvent("Failed", "Failed to insert values into table '" + tableName + "' in database '" + currDBName + "'.");
        }
        return success;
    }

    public boolean updateTable(String tableName, String column, String value, String conditionColumn, String conditionValue) throws CustomException {
        if (!tables.contains(tableName)) {
            logger.logEvent("Failed", "Table '" + tableName + "' doesn't exist in database '" + currDBName + "'.");
            IO.println("Table '" + tableName + "' doesn't exists in database '" + currDBName + "'.");
            return false;
        }
        boolean success = table.update(tableName, column, value, conditionColumn, conditionValue, currDBName);
        if (!success) {
            logger.logEvent("Failed", "Failed to update table '" + tableName + "' in database '" + currDBName + "'.");
        }
        return success;
    }

    public boolean deleteTable(String tableName, String conditionColumn, String conditionValue) throws CustomException {
        if (!tables.contains(tableName)) {
            logger.logEvent("Failed", "Table '" + tableName + "' doesn't exist in database '" + currDBName + "'.");
            IO.println("Table '" + tableName + "' doesn't exists in database '" + currDBName + "'.");
            return false;
        }
        boolean success = table.delete(tableName, conditionColumn, conditionValue, currDBName);
        if (!success) {
            logger.logEvent("Failed", "Failed to delete from table '" + tableName + "' in database '" + currDBName + "'.");
        }
        return success;
    }

    public List<Map<String, Object>> selectTable(String tableName, List<String> columns, String conditionColumn, String conditionValue) throws CustomException {
        if (!tables.contains(tableName)) {
            logger.logEvent("Failed", "Table '" + tableName + "' doesn't exist in database '" + currDBName + "'.");
            IO.println("Table '" + tableName + "' doesn't exists in database '" + currDBName + "'.");
            return null;
        }
        
        this.table.setCurrDBName(currDBName);
        List<Map<String, Object>> results = table.select(tableName, columns, conditionColumn, conditionValue, currDBName);
        if (results.isEmpty()) {

        }
        return results;
    }

    public String getDatabaseState() {
        StringBuilder state = new StringBuilder("State= Database: ").append(currDBName).append("| Tables: ");
        for (String tableName : tables) {
            int recordCount = table.getRecordCount(tableName, currDBName);
            state.append("(").append(tableName).append(", ").append(recordCount).append(" records), ");
        }
        return state.toString();
    }

    public boolean readDatabase(String dbName, List<String> tableNames, List<List<Attribute>> attributes, List<Cardinality> cardinalities) {
        if (dbName == null || dbName.isBlank()) {
            return false;
        }

        List<String> content = fileOperations.read(transactionManager.getDatabasePath() + "/" + dbName + ".txt");

        if (content.isEmpty()) {
            return true;
        }

        for (int i = 0; i < content.size(); i++) {
            String currLine = content.get(i);

            if (currLine.startsWith("@")) {
                tableNames.add(currLine.substring(1));
                i++;

                List<Attribute> currTableAttributes = new ArrayList<>();

                while (i < content.size() && (content.get(i).startsWith("?") || content.get(i).startsWith("&"))) {
                    currLine = content.get(i);

                    if (currLine.startsWith("&")) {
                        String[] column = currLine.substring(1).split("#");
                        Cardinality currCardinality = new Cardinality();

                        boolean isPKOrUnique = false;
                        boolean isKeyFound = false;

                        for (Attribute attribute : currTableAttributes) {
                            if (attribute.getName().equals(column[0])) {
                                isKeyFound = true;
                                if (attribute.isPrimaryKey() || attribute.isUnique()) {
                                    isPKOrUnique = true;
                                    break;
                                }
                            }
                        }

                        if (!isKeyFound) {
                            IO.println("Incorrect foreign key found!");
                            return false;
                        }

                        currCardinality.setFromTableName(tableNames.get(tableNames.size() - 1));
                        currCardinality.setFromTableColumnName(column[0]);
                        currCardinality.setToTableColumnName(column[1]);
                        currCardinality.setToTableName(column[2]);

                        if (isPKOrUnique) {
                            currCardinality.setCardinality("1:1");
                        } else {
                            currCardinality.setCardinality("M:1");
                        }

                        if (currCardinality.getFromTableName() != null) {
                            cardinalities.add(currCardinality);
                        }
                    } else if (currLine.startsWith("?")) {
                        String[] column = currLine.substring(1).split("#");

                        Attribute attribute = validColumn.checkEntireColumn(column[0], column[1], column[2], column[3], column[4], column[5], currLine.substring(1));

                        if (attribute == null) {
                            return false;
                        }

                        currTableAttributes.add(attribute);
                    }

                    i++;
                }

                attributes.add(currTableAttributes);
                i--;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "Database{" +
                "name='" + currDBName + '\'' +
                ", tables=" + tables +
                '}';
    }

    public void createCopy(){
        this.originalTable = new ArrayList<>(tables);
    }

    public void revertBack(){
        this.tables = this.originalTable;
        this.originalTable = null;
    }

    public String getCurrDBName() {
        return currDBName;
    }

    public void setCurrDBName(String currDBName) {
        this.currDBName = currDBName;
    }

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }
}
