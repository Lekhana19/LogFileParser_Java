package handlers;

import util.SaveJSONFile;

import java.util.*;

public class APMHandler implements Handler {
    private Handler nextHandler;
    private Map<String, List<Double>> metrics = new HashMap<>();

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

    @Override
    public void final_output() {
        Map<String, Map<String, Object>> aggregatedData = aggregateMetrics();
        SaveJSONFile.writeMapToJson(aggregatedData, "apm.json");
        if (nextHandler != null) {
            nextHandler.final_output();
        }
    }

    private void processLog(String log) {
        String[] parts = log.split(" ");
        String metricType = null;
        double value = 0;

        for (String part : parts) {
            if (part.startsWith("metric=")) {
                metricType = part.split("=")[1];
            } else if (part.startsWith("value=")) {
                try {
                    value = Double.parseDouble(part.split("=")[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid number format for value in log: " + log);
                    return; // Skip this log if the value is not a valid double
                }
            }
        }

        if (metricType != null) {
            metrics.putIfAbsent(metricType, new ArrayList<>());
            metrics.get(metricType).add(value);
        }
    }

    public Map<String, Map<String, Object>> aggregateMetrics() {
        Map<String, Map<String, Object>> aggregatedData = new LinkedHashMap<>();

        for (Map.Entry<String, List<Double>> entry : metrics.entrySet()) {
            String metricType = entry.getKey();
            List<Double> values = entry.getValue();
            Collections.sort(values);
            double min = Math.round(values.get(0));
            double max = Math.round(values.get(values.size() - 1));
            double average = Math.round(values.stream().mapToDouble(val -> val).average().orElse(0.0));

            double median;
            int size = values.size();
            if (size % 2 == 0) {
                median = Math.round((values.get(size / 2 - 1) + values.get(size / 2)) / 2.0);
            } else {
                median = Math.round(values.get(size / 2));
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