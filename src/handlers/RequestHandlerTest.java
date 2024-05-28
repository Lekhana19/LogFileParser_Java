package handlers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class RequestHandlerTest {

    @Test
    public void testRequestHandling() {
        // Initialize handler
        RequestHandler handler = new RequestHandler();

        // Define logs, including correct and potentially corrupt entries
        String[] logs = {
                "request_url=/api/data response_time_ms=1500 response_status=200",
                "request_url=/api/data response_time_ms=1000 response_status=200",
                "request_url=/api/data response_time_ms=2500 response_status=400",
                "request_method=GET request_url=/api/users response_time_ms=2200 response_status=200",
                "response_time_ms=500",
                "request_url=/api/data response_time_ms=notanumber response_status=200",
                "request_url=/api/users response_time_ms=2500 response_status=200",
                "request_url=/api/users response_status=500"
        };

        // Process each log
        for (String log : logs) {
            System.out.println("Processing log: " + log); // Debug output
            handler.handle(log);
        }

        // Aggregate data and assert results
        Map<String, Map<String, Object>> actualData = handler.aggregateResponseData();

        // Print actual data for debugging
        actualData.forEach((key, value) -> System.out.println("Aggregated Data: " + key + " => " + value));

        // Define expected output
        Map<String, Map<String, Object>> expectedData = new HashMap<>();
        expectedData.put("/api/data", Map.of(
                "response_times", Map.of(
                        "min", 1000.0, "50_percentile", 1500.0, "90_percentile", 2500.0,
                        "95_percentile", 2500.0, "99_percentile", 2500.0, "max", 2500.0),
                "status_codes", Map.of("2XX", 2, "4XX", 1)
        ));
        expectedData.put("/api/users", Map.of(
                "response_times", Map.of(
                        "min", 2200.0, "50_percentile", 2350.0, "90_percentile", 2500.0,
                        "95_percentile", 2500.0, "99_percentile", 2500.0, "max", 2500.0),
                "status_codes", Map.of("2XX", 1)  // Assuming only one valid 2XX response
        ));

        // Assert that the actual data matches the expected data
        assertEquals(expectedData, actualData, "The aggregated data does not match the expected output.");
    }
}
