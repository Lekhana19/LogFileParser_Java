package handlers;
import util.SaveJSONFile;

import java.util.*;

public class ApplicationHandler implements Handler {
    private Handler nextHandler;
    private Map<String, Integer> logLevelCount = new HashMap<>();

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
        for (String part : parts) {
            if (part.startsWith("level=")) {
                String level = part.split("=")[1];
                logLevelCount.put(level, logLevelCount.getOrDefault(level, 0) + 1);
            }
        }
    }

    @Override
    public void final_output() {
        Map<String, Object> aggregatedData = new LinkedHashMap<>();
        Map<String, Integer> logCounts = new LinkedHashMap<>(logLevelCount);
        // Printing the aggregated data as JSON
        //System.out.println(logCounts);
        SaveJSONFile.writeMapToJson(logCounts,"application.json");
        //call the next handler in the chain
        if (nextHandler != null) {
            nextHandler.final_output();
        }
    }
}