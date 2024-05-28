package handlers;

import util.SaveJSONFile;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class APMHandler implements Handler {
    private Handler nextHandler;
    private Map<String, List<Double>> metrics = new HashMap<>();
    private FileWriter errorLogWriter; // FileWriter to write errors to a file

    public APMHandler() {
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
        if (log.contains("metric=")) {
            processLog(log);
        } else if (nextHandler != null) {
            nextHandler.handle(log);
        }
    }

    private void processLog(String log) {
        String[] parts = log.split(" ");
        String metricType = null;
        Double value = null;

        try {
            for (String part : parts) {
                if (part.startsWith("metric=")) {
                    metricType = part.split("=")[1];
                } else if (part.startsWith("value=")) {
                    value = Double.parseDouble(part.split("=")[1]);
                }
            }
        } catch (NumberFormatException e) {
            logError("Skipping invalid log entry (number format): " + log);
            return; // Skip this log if parsing fails
        }

        if (metricType == null || value == null) {
            logError("Skipping incomplete log entry: " + log);
            return; // Skip processing this log
        }

        metrics.putIfAbsent(metricType, new ArrayList<>());
        metrics.get(metricType).add(value);
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
        Map<String, Map<String, Object>> aggregatedData = aggregateMetrics();
        SaveJSONFile.writeMapToJson(aggregatedData, "apm.json");

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

    Map<String, Map<String, Object>> aggregateMetrics() {
        Map<String, Map<String, Object>> aggregatedData = new LinkedHashMap<>();

        for (Map.Entry<String, List<Double>> entry : metrics.entrySet()) {
            String metricType = entry.getKey();
            List<Double> values = entry.getValue();
            Collections.sort(values);
            double min = values.get(0);
            double max = values.get(values.size() - 1);
            double average = values.stream().mapToDouble(val -> val).average().orElse(0.0);

            double median;
            int size = values.size();
            if (size % 2 == 0) {
                median = (values.get(size / 2 - 1) + values.get(size / 2)) / 2.0;
            } else {
                median = values.get(size / 2);
            }

            Map<String, Object> metricData = new LinkedHashMap<>();
            metricData.put("min", min);
            metricData.put("average", average);
            metricData.put("median", median);
            metricData.put("max", max);

            aggregatedData.put(metricType, metricData);
        }

        return aggregatedData;
    }
}
