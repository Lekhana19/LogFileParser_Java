package handlers;


import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class APMHandlerTest {


    @Test
    public void testAggregateMetrics() {
        // Create a sample log data
        List<String> logs = new ArrayList<>();
        logs.add("metric=memory value=30.5");
        logs.add("metric=memory value=25.6");
        logs.add("metric=cpu value=60.3");
        logs.add("metric=cpu value=70.9");


        // Create an instance of APMHandler
        Handler apmHandler = new APMHandler();


        // Process logs
        for (String log : logs) {
            apmHandler.handle(log);
        }


        // Call aggregateMetrics to get aggregated data
        Map<String, Map<String, Object>> aggregatedData = ((APMHandler) apmHandler).aggregateMetrics();


        // Convert all values to integers
        aggregatedData = convertToInteger(aggregatedData);


        // Expected aggregated data
        Map<String, Map<String, Object>> expectedData = new HashMap<>();
        Map<String, Object> memoryData = new HashMap<>();
        memoryData.put("min", 26);
        memoryData.put("average", 28);
        memoryData.put("median", 28);
        memoryData.put("max", 31);
        expectedData.put("memory", memoryData);
        Map<String, Object> cpuData = new HashMap<>();
        cpuData.put("min", 60);
        cpuData.put("average", 66);
        cpuData.put("median", 66);
        cpuData.put("max", 71);
        expectedData.put("cpu", cpuData);


        // Check if the aggregated data matches the expected data
        assertEquals(expectedData, aggregatedData);
    }


    private Map<String, Map<String, Object>> convertToInteger(Map<String, Map<String, Object>> data) {
        Map<String, Map<String, Object>> convertedData = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : data.entrySet()) {
            Map<String, Object> convertedEntry = new HashMap<>();
            for (Map.Entry<String, Object> subEntry : entry.getValue().entrySet()) {
                if (subEntry.getValue() instanceof Double) {
                    double value = (Double) subEntry.getValue();
                    convertedEntry.put(subEntry.getKey(), (int) Math.round(value));
                } else {
                    convertedEntry.put(subEntry.getKey(), subEntry.getValue());
                }
            }
            convertedData.put(entry.getKey(), convertedEntry);
        }
        return convertedData;
    }
}
