package com.csci5408;

import com.csci5408.Authentication.Authentication;
import com.csci5408.Engine.Manager;
import com.csci5408.Engine.QueryProcessor;
import com.csci5408.Export.DatabaseExporter;
import com.csci5408.Export.ERD;
import com.csci5408.Models.User;
import com.csci5408.OS.FileOperations;
import com.csci5408.OS.IO;
import com.csci5408.OS.Logger;
import com.csci5408.Utils.CustomException;

import java.nio.file.Paths;
import java.util.*;


public class Main {

    public static void main(String[] args) {

        checkDirectory();

        Authentication authentication = new Authentication();
        Logger logger = Logger.getInstance();

        try {
            while (true) {
                IO.println("\nMenu:\n1. Login\n2. Register\n3. Exit");

                int choice = -1;

                try {
                    choice = Integer.parseInt(IO.readLine());
                } catch (NumberFormatException e) {
                    IO.println("Invalid choice!");
                    continue;
                }

                switch (choice) {
                    case 1:
                        IO.print("Enter username: ");
                        String userID = IO.readLine();
                        IO.print("Enter password: ");
                        String password = IO.readLine();

                        boolean isValidUser = authentication.loginUser(userID, password);
                        if (isValidUser) {
                            menu(authentication, userID);
                        }
                        break;

                    case 2:
                        IO.print("Enter new username: ");
                        String registerID = IO.readLine();
                        IO.print("Enter password: ");
                        String registerPassword = IO.readLine();

                        List<String> securityQA = new ArrayList<>();

                        for (int i=0;i<2;i++){
                            IO.print("Enter security question: ");
                            String question = IO.readLine();
                            IO.print("Enter answer: ");
                            String answer = IO.readLine();

                            securityQA.add(question);
                            securityQA.add(answer);
                        }

                        User user = new User(registerID, registerPassword, securityQA);
                        authentication.registerUser(user);
                        logger.logEvent("Successful", "User '" + registerID + "' registered successfully.");
                        break;

                    case 3:
                        IO.println("Exiting.....");
                        logger.logEvent("Successful", "Application exited.");
                        return;

                    default:
                        IO.println("Invalid choice!");
                        break;
                }
            }
        } catch (Exception e) {
            logger.logCrashReport("Failed", "An unexpected error occurred", e);
        }
    }

    public static void menu(Authentication authentication, String userID) throws CustomException {
        while (true){
            IO.println("\n1. Write Queries" +
                    "\n2. Export Data and Structure" +
                    "\n3. ERD" +
                    "\n4. Go back");

            int input = -1;

            try {
                String choice = IO.readLine();
                input = Integer.parseInt(choice);
            }catch (NumberFormatException e){
                IO.println("Invalid choice!");
                continue;
            }

            switch (input){
                case 1:
                    execQueries();
                    break;
                case 2:
                    DatabaseExporter exporter = new DatabaseExporter();
                    IO.println("Enter database name: ");
                    exporter.exportDatabase(IO.readLine());
                    break;
                case 3:
                    createERD();
                    break;
                case 4:
                    authentication.logOutUser(userID);
                    return;
                default:
                    IO.println("Invalid choice!");
                    break;
            }
        }
    }

    public static void createERD(){
        ERD erd = new ERD();
        erd.drawERD();
    }

    public static void execQueries() throws CustomException {
        Manager manager = new Manager();
        QueryProcessor queryProcessor = new QueryProcessor(manager);

        StringBuilder queryBuilder = new StringBuilder();

        IO.println("Enter your SQL queries (type 'EXIT' to end):");

        while (true) {
            IO.print("SQL> ");

            String line = IO.readLine();

            if (line.equalsIgnoreCase("EXIT")) {
                break;
            }

            queryBuilder.append(line).append(" ");

            // If the line ends with a semicolon, process the query
            if (line.trim().endsWith(";")) {
                String query = queryBuilder.toString().trim();
                queryBuilder.setLength(0); // Clear the StringBuilder

                List<String> queries = new ArrayList<>();
                queries.add(query);

                queryProcessor.processQueries(queries);
            }else {
                IO.println("SQL Syntax Error: ';' missing at the end of line");
            }
        }
    }


    public static void checkDirectory(){
        FileOperations fileOperations = new FileOperations();
        fileOperations.createDirectory("DataStore");
        fileOperations.createDirectory("DataStore/logs");
        fileOperations.createFile("DataStore/metadata.txt");
        fileOperations.createFile("DataStore/User_Profile.txt");
    }


}
