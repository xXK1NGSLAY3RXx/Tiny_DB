package com.csci5408.Export;

import com.csci5408.Engine.Database;
import com.csci5408.Engine.TransactionManager;
import com.csci5408.OS.IO;
import com.csci5408.Models.Attribute;
import com.csci5408.Models.Cardinality;
import com.csci5408.OS.FileOperations;
import com.csci5408.Utils.ValidColumn;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ERD {

    private FileOperations fileOperations;
    private List<String> databases;
    private List<List<Attribute>> tableAttributes;
    private List<String> tableNames;
    private String dbName;
    private List<Cardinality> cardinalities;
    private ValidColumn validColumn;

    public ERD(){
        fileOperations = new FileOperations();
        databases = new ArrayList<>();
        dbName = "";
        cardinalities = new ArrayList<>();
        tableNames = new ArrayList<>();
        tableAttributes = new ArrayList<>();
        validColumn = new ValidColumn();
    }


    public boolean loadMetaData(){

        databases.clear();

        List<String> content = fileOperations.read("DataStore/metadata.txt");

        if(content == null || content.isEmpty()){
            IO.println("Do not have any database!");
            return false;
        }

        IO.println("");
        IO.println("");
        IO.println("List of Databases:");
        for (String currLine : content){
            databases.add(currLine);
            IO.println(currLine);
        }
        IO.println("");
        IO.println("");

        return true;
    }

    public void drawERD(){

        if (!loadMetaData()){
            return;
        }

        IO.print("Enter Database Name: ");
        String inputDBName = IO.readLine();

        if (!databases.contains(inputDBName)){
            IO.println("Database Not Found!");
            return;
        }

        dbName = inputDBName;

        if (!loadDatabaseFile()){
            IO.println("Return");
            return;
        }

        createTxtFile();

        return;
    }

    private void createTxtFile(){

        List<String> content = new ArrayList<>();

        for (int i = 0; i< tableNames.size(); i++){

            content.add(tableNames.get(i));

            List<Attribute> attribute = tableAttributes.get(i);

            for (Attribute currAttr : attribute){

                Cardinality cardinality = checkIsForeignkey(currAttr, tableNames.get(i));

                if (currAttr.isPrimaryKey()){
                    if (cardinality != null){
                        content.add(currAttr.getName() + " (PK) (" + cardinality.getCardinality() + ") with " + cardinality.getToTableName());
                    }
                    else {
                        content.add(currAttr.getName() + " (PK)");
                    }
                }else {
                    if (cardinality != null){
                        content.add(currAttr.getName() + "(" + cardinality.getCardinality() + ") with " + cardinality.getToTableName());
                    }
                    else{
                        content.add(currAttr.getName());
                    }
                }
            }

            content.add("\n");
        }

        if (!fileOperations.isDirectoryFileExist("DataStore/" + dbName + "/ERD")){
            fileOperations.createDirectory("DataStore/" + dbName + "/ERD");
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyHHmmss");
        String timestamp = now.format(formatter);

        fileOperations.write("DataStore/" + dbName + "/ERD/" + dbName + "_"+ timestamp + ".txt", content, false);
        IO.print("ERD Printed Successfully!");
    }

    private Cardinality checkIsForeignkey(Attribute attribute, String tableName){

        for (Cardinality cardinality : cardinalities){
            if (cardinality.getFromTableColumnName().equals(attribute.getName()) && cardinality.getFromTableName().equals(tableName)){
                return cardinality;
            }
        }

        return null;
    }


    private boolean loadDatabaseFile(){

        if (dbName == null || dbName.isBlank()){
            IO.println("No Database Selected!");
            return false;
        }

        tableNames.clear();
        tableAttributes.clear();
        cardinalities.clear();

        Database database = new Database();

        if (!readDatabase(dbName, tableNames, tableAttributes, cardinalities)){
            tableNames.clear();
            tableAttributes.clear();
            cardinalities.clear();
            return false;
        }

        return true;
    }

    public boolean readDatabase(String dbName, List<String> tableNames, List<List<Attribute>> attributes, List<Cardinality> cardinalities) {

        if (dbName == null || dbName.isBlank()) {
            return false;
        }

        List<String> content = fileOperations.read("DataStore" + "/" + dbName + "/" + dbName + ".txt");

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

}
