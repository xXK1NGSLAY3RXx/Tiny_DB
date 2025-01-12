package com.csci5408.Engine;

import com.csci5408.OS.Logger;
import com.csci5408.Utils.CustomException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

public class TransactionManager {
    private static volatile TransactionManager instance;
    private String currentDBName;
    private String originalDatabasePath;
    private String tempDatabasePath;
    private boolean inTransaction;

    public TransactionManager() {
        this.inTransaction = false;
    }

    public void setCurrentDBName(String currentDBName) {
        this.currentDBName = currentDBName;
        this.originalDatabasePath = "DataStore/" + currentDBName;
    }

    public static TransactionManager getInstance() {
        if (instance == null) {
            synchronized (TransactionManager.class) {
                if (instance == null) {
                    instance = new TransactionManager();
                }
            }
        }
        return instance;
    }

    public void startTransaction() throws IOException, CustomException {
        if (inTransaction) {
            System.out.println("Transaction already in progress");
            throw new CustomException("Transaction already in progress");
        }
        if(currentDBName == null) {
            System.out.println("No database set currently");
            throw new CustomException("No database set currently");
        }

        this.tempDatabasePath = "DataStore/" + currentDBName + "_temp";
        copyDirectory(Paths.get(originalDatabasePath), Paths.get(tempDatabasePath));

        inTransaction = true;
    }

    public void commitTransaction() throws IOException, CustomException {
        if (!inTransaction) {
            throw new CustomException("No transaction in progress");
        }

        deleteDirectory(Paths.get(originalDatabasePath));
        Files.move(Paths.get(tempDatabasePath), Paths.get(originalDatabasePath), StandardCopyOption.ATOMIC_MOVE);
        inTransaction = false;
    }

    public void rollbackTransaction() throws IOException, CustomException {
        if (!inTransaction) {
            throw new CustomException("No transaction in progress");
        }

        deleteDirectory(Paths.get(tempDatabasePath));
        inTransaction = false;
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(src -> {
            Path dest = target.resolve(source.relativize(src));
            try {
                Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void deleteDirectory(Path path) throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public String getDatabasePath() {
        return inTransaction ? tempDatabasePath : originalDatabasePath;
    }
}
