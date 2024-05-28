package handlers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ApplicationHandlerTest {


    @Test
    public void testAggregateLogLevels() throws IOException, JSONException {
        // Create a sample log data
        String[] logs = {
                "level=INFO",
                "level=ERROR",
                "level=WARN",
                "level=INFO",
                "level=DEBUG",
                "level=ERROR"
        };


        // Create an instance of ApplicationHandler
        Handler applicationHandler = new ApplicationHandler();


        // Process logs
        for (String log : logs) {
            applicationHandler.handle(log);
        }


        // Call final_output to save aggregated data to file
        applicationHandler.final_output();


        // Read the content of the saved file
        String filePath = "application.json";
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }


        // Parse JSON content into a map
        JSONObject jsonObject = new JSONObject(content.toString());
        Iterator<String> keys = jsonObject.keys();
        Map<String, Integer> aggregatedData = new HashMap<>();
        while (keys.hasNext()) {
            String key = keys.next();
            aggregatedData.put(key, jsonObject.getInt(key));
        }


        // Expected aggregated data
        Map<String, Integer> expectedData = new HashMap<>();
        expectedData.put("INFO", 2);
        expectedData.put("ERROR", 2);
        expectedData.put("WARN", 1);
        expectedData.put("DEBUG", 1);


        // Check if the aggregated data matches the expected data
        assertEquals(expectedData, aggregatedData);
    }
}
