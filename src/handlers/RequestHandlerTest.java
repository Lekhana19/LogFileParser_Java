package handlers;
import org.junit.jupiter.api.Test;


import java.util.*;


import static org.junit.jupiter.api.Assertions.assertEquals;


public class RequestHandlerTest {


    @Test
    public void testAggregateResponseData() {
        // Create a sample log data
        String[] logs = {
                "request_url=/api/data response_time_ms=1000 response_status=200",
                "request_url=/api/data response_time_ms=1500 response_status=500",
                "request_url=/api/users response_time_ms=2000 response_status=200",
                "request_url=/api/users response_time_ms=2500 response_status=200",
                "request_url=/api/data response_time_ms=1200 response_status=404"
        };


        // Create an instance of RequestHandler
        Handler requestHandler = new RequestHandler();


        // Process logs
        for (String log : logs) {
            requestHandler.handle(log);
        }


        // Call final_output to get aggregated data
        Map<String, Map<String, Object>> aggregatedData = new LinkedHashMap<>();
        ((RequestHandler) requestHandler).final_output();


        // Expected aggregated data
        Map<String, Map<String, Object>> expectedData = new LinkedHashMap<>();
        Map<String, Object> data1 = new LinkedHashMap<>();
        data1.put("response_times", Map.of("min", 1000.0, "50_percentile", 1200.0, "90_percentile", 2500.0, "95_percentile", 2500.0, "99_percentile", 2500.0, "max", 2500.0));
        data1.put("status_codes", Map.of("2XX", 3, "4XX", 1));
        expectedData.put("/api/data", data1);


        Map<String, Object> data2 = new LinkedHashMap<>();
        data2.put("response_times", Map.of("min", 2000.0, "50_percentile", 2250.0, "90_percentile", 2500.0, "95_percentile", 2500.0, "99_percentile", 2500.0, "max", 2500.0));
        data2.put("status_codes", Map.of("2XX", 2));
        expectedData.put("/api/users", data2);


        // Check if the aggregated data matches the expected data
        assertEquals(expectedData, aggregatedData);
    }
}
