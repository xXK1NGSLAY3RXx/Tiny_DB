package com.csci5408.Engine;

import com.csci5408.Models.Attribute;
import com.csci5408.Models.Cardinality;
import com.csci5408.OS.FileOperations;
import com.csci5408.OS.IO;
import com.csci5408.Utils.ValidColumn;

import java.util.ArrayList;
import java.util.List;

public class ValidateForeignKey {

    private List<List<Attribute>> tableAttributes;
    private List<String> tableNames;
    private List<Cardinality> cardinalities;
    private String currDBName;
    private FileOperations fileOperations;
    private ValidColumn validColumn;

    public ValidateForeignKey() {
        fileOperations = new FileOperations();
        currDBName = "";
        tableNames = new ArrayList<>();
        tableAttributes = new ArrayList<>();
        validColumn = new ValidColumn();
        cardinalities = new ArrayList<>();
    }

    private void printTableAttributes(List<Attribute> attributes){
        for (Attribute attribute : attributes){
            IO.println(attribute.getName());
        }
    }

    private void printTablesName(){
        for (String currTable : tableNames){
            IO.println(currTable);
        }
    }


    public List<String> getForeignKeyContent(String tableName, List<Attribute> attributes){

        if (!readDatabase()){
            return null;
        }

        List<String> content = new ArrayList<>();

        boolean isExit = false;

        while (!isExit){

            IO.print("Enter Field Name: ");
            String insertTableFieldName = IO.readLine();

            if (insertTableFieldName.equalsIgnoreCase("exit")){
                isExit = true;
                continue;
            }

            boolean isFiledNameValid = false;
            for (Attribute attribute : attributes){
                if (attribute.getName().equals(insertTableFieldName)){
                    isFiledNameValid = true;
                }
            }

            if (!isFiledNameValid){
                IO.println("Invalid Field Name!");
                continue;
            }

            printTablesName();

            IO.print("Enter Table Name: ");
            String foreignTableName = IO.readLine();

            if (!tableNames.contains(foreignTableName)){
                IO.println("Invalid Table Name!");
                continue;
            }

            List<Attribute> choosingAttributes = new ArrayList<>();

            for (int i = 0; i< tableAttributes.size(); i++){
                if (tableNames.get(i).equals(foreignTableName)){
                    choosingAttributes = tableAttributes.get(i);
                    printTableAttributes(choosingAttributes);
                    break;
                }
            }

            IO.print("Enter Field Name: ");
            String foreignKey = IO.readLine();

            boolean isKeyValid = false;

            for (Attribute attribute : choosingAttributes){
                if (attribute.getName().equals(foreignKey)){
                    isKeyValid = true;
                }
            }

            if (!isKeyValid){
                IO.println("Invalid Foreign Key Name!");
                continue;
            }

            content.add("&" +  insertTableFieldName + "#" + foreignKey + "#" + foreignTableName);
            IO.println("Foreign Key Added Successfully!");
        }

        return content;
    }


    private boolean readDatabase(){

        if (currDBName == null || currDBName.isBlank()){
            return false;
        }

        tableNames.clear();
        tableAttributes.clear();
        cardinalities.clear();

        Database database = new Database();

        if (!database.readDatabase(currDBName, tableNames, tableAttributes, cardinalities)){
            tableNames.clear();
            tableAttributes.clear();
            cardinalities.clear();
            return false;
        }

        return true;
    }


    public String getCurrDBName() {
        return currDBName;
    }

    public void setCurrDBName(String currDBName) {
        this.currDBName = currDBName;
    }

    public List<List<Attribute>> getTableAttributes() {
        return tableAttributes;
    }

    public void setTableAttributes(List<List<Attribute>> tableAttributes) {
        this.tableAttributes = tableAttributes;
    }

    public List<String> getExistingTableNames() {
        return tableNames;
    }

    public void setExistingTableNames(List<String> existingTableNames) {
        this.tableNames = existingTableNames;
    }
}
