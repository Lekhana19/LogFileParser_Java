package handlers;

import util.SaveJSONFile;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class RequestHandler implements Handler {
    private Handler nextHandler;
    private Map<String, List<Integer>> responseTimes = new HashMap<>();
    private Map<String, Map<String, Integer>> statusCounts = new HashMap<>();
    private FileWriter errorLogWriter; // FileWriter to write errors to a file

    public RequestHandler() {
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
        if (log.contains("request_method=")) {
            processLog(log);
        } else if (nextHandler != null) {
            nextHandler.handle(log);
        }
    }

    private void processLog(String log) {
        String[] parts = log.split(" ");
        String url = null;
        Integer responseTime = null;
        String statusCode = null;

        try {
            for (String part : parts) {
                if (part.startsWith("request_url=")) {
                    url = part.split("=")[1];
                    url = url.replace("\"", ""); // Remove double quotes
                    url = url.replace("\\", ""); // Remove backslashes
                } else if (part.startsWith("response_time_ms=")) {
                    responseTime = Integer.parseInt(part.split("=")[1]);
                } else if (part.startsWith("response_status=")) {
                    statusCode = part.split("=")[1].substring(0, 1) + "XX";
                }
            }
        } catch (NumberFormatException e) {
            logError("Skipping invalid log entry (number format): " + log);
            return; // Skip this log if parsing fails
        }

        if (url == null || responseTime == null || statusCode == null) {
            logError("Skipping incomplete log entry: " + log);
            return; // Skip processing this log
        }

        responseTimes.putIfAbsent(url, new ArrayList<>());
        responseTimes.get(url).add(responseTime);

        statusCounts.putIfAbsent(url, new HashMap<>());
        statusCounts.get(url).put(statusCode, statusCounts.get(url).getOrDefault(statusCode, 0) + 1);
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
        Map<String, Map<String, Object>> aggregatedData = aggregateResponseData();
        SaveJSONFile.writeMapToJson(aggregatedData, "request.json");

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

    Map<String, Map<String, Object>> aggregateResponseData() {
        Map<String, Map<String, Object>> aggregatedData = new LinkedHashMap<>();

        for (Map.Entry<String, List<Integer>> entry : responseTimes.entrySet()) {
            String url = entry.getKey();
            List<Integer> times = entry.getValue();
            Collections.sort(times);
            double min = times.get(0);
            double max = times.get(times.size() - 1);
            double percentile50 = getPercentile(times, 50);
            double percentile90 = getPercentile(times, 90);
            double percentile95 = getPercentile(times, 95);
            double percentile99 = getPercentile(times, 99);

            Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("min", min);
            responseData.put("50_percentile", percentile50);
            responseData.put("90_percentile", percentile90);
            responseData.put("95_percentile", percentile95);
            responseData.put("99_percentile", percentile99);
            responseData.put("max", max);

            Map<String, Integer> statusData = statusCounts.getOrDefault(url, new HashMap<>());

            Map<String, Object> urlData = new LinkedHashMap<>();
            urlData.put("response_times", responseData);
            urlData.put("status_codes", statusData);

            aggregatedData.put(url, urlData);
        }

        return aggregatedData;
    }

    private double getPercentile(List<Integer> times, double percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * times.size()) - 1;
        return times.get(index);
    }
}
