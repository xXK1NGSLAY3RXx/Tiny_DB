package com.csci5408.OS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileOperations {

    public List<String> read(String location){

        List<String> content = new ArrayList<>();

        if (!isDirectoryFileExist("DataStore")){
            createDirectory("DataStore");
        }

        if (!isDirectoryFileExist(location)){
            createFile(location);
        }

        File file = new File(location);

        try {
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()){
                content.add(scanner.nextLine());
            }

            scanner.close();
        }
        catch (FileNotFoundException fileNotFoundException){
            IO.println("User_Profile.txt File Not Found!");
            return null;
        }

        return content;
    }

    public boolean write(String location, List<String> content, boolean isOverWrite){

        boolean isFilePresent = true;

        if (!isDirectoryFileExist("DataStore")){
            createDirectory("DataStore");
        }

        if (!isDirectoryFileExist(location)){
            isFilePresent = false;
            createFile(location);
        }

        if (isFilePresent){
            if (isOverWrite){
                return overWrite(location, content);
            }
            else {
                return append(location, content);
            }
        }

        try (FileWriter fileWriter = new FileWriter(location, false)){
            for (String currLine : content){
                fileWriter.write(currLine);
                fileWriter.write("\n");
            }
        }
        catch (IOException ioException){
            IO.println("Failed to write file!");
            return false;
        }

        return true;
    }

    public boolean append(String location, List<String> content){
        try (FileWriter fileWriter = new FileWriter(location, true)){
            for (String currLine : content){
                fileWriter.write(currLine);
                fileWriter.write("\n");
            }
        }
        catch (IOException ioException){
            IO.println("Failed to write file! " + ioException.getMessage());
            return false;
        }

        return true;
    }

    public boolean overWrite(String location, List<String> content){
        try (FileWriter fileWriter = new FileWriter(location, false)){
            for (String currLine : content){
                fileWriter.write(currLine);
                fileWriter.write("\n");
            }
        }
        catch (IOException ioException){
            IO.println("Failed to write file!");
            return false;
        }

        return true;
    }

    public boolean isDirectoryFileExist(String location){
        File file = new File(location);
        return file.exists();
    }

    public void createFile(String location){
        File file = new File(location);
        try {
            file.createNewFile();
        }
        catch (IOException ioException){
            IO.println("Failed to Create File! " + ioException.getMessage());
        }
    }

    public void createDirectory(String location){
        try {
            Files.createDirectories(Paths.get(location));
        } catch (IOException ioException) {
            IO.println("Failed to create directory!");
        }
    }

    public void deleteDirectoryOrFile(String location){
        Path path = Paths.get(location);
        try {
            Files.delete(path);
            IO.println("Deleted successfully");
        } catch (IOException e) {
            IO.println("Failed to delete: " + e.getMessage());
        }
    }

}
