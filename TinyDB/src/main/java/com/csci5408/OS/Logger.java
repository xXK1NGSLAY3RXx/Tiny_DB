package com.csci5408.OS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static Logger logger;
    private String logFilePath;

    private Logger() {

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyy");
        String timestamp = now.format(formatter);

        this.logFilePath = Paths.get("DataStore/logs/", timestamp + ".txt").toString();
    }

    public static Logger getInstance() {
        if (logger == null) {
            synchronized (Logger.class) {
                if (logger == null) {
                    logger = new Logger();
                }
            }
        }
        return logger;
    }

    public void setFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public boolean log(String status, String logMessage) {
        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write("[" + status + "] [" + new Timestamp(System.currentTimeMillis()) + "] " + logMessage);
            bufferedWriter.newLine();
            bufferedWriter.close();
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public void logGeneral(String status, String message) {
        log(status, message);
    }

    public void logEvent(String status, String message) {
        log(status, message);
    }

    public void logQuery(String status, String message) {
        log(status, message);
    }

    public void logCrashReport(String status, String message, Exception e) {
        log(status, message + " Exception: " + e.getMessage());
    }
}
