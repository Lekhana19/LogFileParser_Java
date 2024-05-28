package handlers;

import util.SaveJSONFile;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ApplicationHandler implements Handler {
    private Handler nextHandler;
    private Map<String, Integer> logLevelCount = new HashMap<>();
    private FileWriter errorLogWriter; // FileWriter to write errors to a file

    public ApplicationHandler() {
        try {
            errorLogWriter = new FileWriter("error_logs.txt", true); // Open the file in append mode
        } catch (IOException e) {
            System.err.println("Error opening the error log file: " + e.getMessage());
        }
    }

    @Override
    public void setNext(Handler handler) {
        this.nextHandler = handler;
    }

    @Override
    public void handle(String log) {
        if (log.contains("level=")) {
            processLog(log);
        } else if (nextHandler != null) {
            nextHandler.handle(log);
        }
    }

    private void processLog(String log) {
        String[] parts = log.split(" ");
        String level = null;

        for (String part : parts) {
            if (part.startsWith("level=")) {
                level = part.split("=")[1];
                if (level == null || level.isEmpty()) {
                    logError("Skipping log due to missing or null level: " + log);
                    return; // Skip this log
                }
                logLevelCount.put(level, logLevelCount.getOrDefault(level, 0) + 1);
            }
        }

        if (level == null) {
            logError("Skipping log with incomplete data: " + log);
            return; // Skip processing this log
        }
    }

    private void logError(String message) {
        try {
            errorLogWriter.write(message + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to error log file: " + e.getMessage());
        }
    }

    @Override
    public void final_output() {
        Map<String, Object> aggregatedData = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : logLevelCount.entrySet()) {
            aggregatedData.put(entry.getKey(), entry.getValue());
        }
        SaveJSONFile.writeMapToJson(aggregatedData, "application.json");

        try {
            if (errorLogWriter != null) {
                errorLogWriter.close(); // Close the FileWriter to flush and release system resources
            }
        } catch (IOException e) {
            System.err.println("Error closing the error log file: " + e.getMessage());
        }

        if (nextHandler != null) {
            nextHandler.final_output();
        }
    }
}
