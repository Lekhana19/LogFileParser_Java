package handlers;
import util.SaveJSONFile;

import java.util.*;

public class RequestHandler implements Handler {
    private Handler nextHandler;
    private Map<String, List<Integer>> responseTimes = new HashMap<>();
    private Map<String, Map<String, Integer>> statusCounts = new HashMap<>();

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
        int responseTime = 0;
        String statusCode = null;

        for (String part : parts) {
            if (part.startsWith("request_url=")) {
                url = part.split("=")[1];
                url = url.replace("\"", ""); // Remove double quotes
                url = url.replace("\\", ""); // Remove backslashes
            } else if (part.startsWith("response_time_ms=")) {
                try {
                    responseTime = Integer.parseInt(part.split("=")[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid number format for response time in log: " + log);
                    return; // Skip this log if the response time is not a valid integer
                }
            } else if (part.startsWith("response_status=")) {
                statusCode = part.split("=")[1].substring(0, 1) + "XX";
            }
        }

        if (url != null) {
            responseTimes.putIfAbsent(url, new ArrayList<>());
            responseTimes.get(url).add(responseTime);

            statusCounts.putIfAbsent(url, new HashMap<>());
            statusCounts.get(url).put(statusCode, statusCounts.get(url).getOrDefault(statusCode, 0) + 1);
        }
    }

    public Map<String, Map<String, Object>> aggregateResponseData() {
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

        // Printing the aggregated data as JSON
        return aggregatedData;
    }

    private double getPercentile(List<Integer> times, double percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * times.size()) - 1;
        return times.get(index);
    }
    @Override
    public void final_output(){
        Map<String, Map<String, Object>> aggregatedData = aggregateResponseData();
        SaveJSONFile.writeMapToJson(aggregatedData,"request.json");
        if (nextHandler != null) {
            nextHandler.final_output();
        }
    }

}
