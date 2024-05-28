# Log Processing Application

## Problem Statement
The problem we are solving is log processing, where log files have several types of log entries. There are three categories of logs:
- APM Logs: These contain performance-related metrics such as CPU, memory, and disk usage.
- Application Logs: These contain operational state information like errors, warnings, and informational messages.
- Request Logs: These capture HTTP requests made to the application, including request method, URL, response status, and response times, for analyzing user interaction and server performance.

## Design Patterns
The design pattern used in this application is the Chain of Responsibility pattern. This pattern allows us to streamline the processing and parsing of diverse log entries by passing a log entry through a chain of handlers, each capable of processing a specific type of log. This modular approach makes the system highly extensible and maintainable, as new log types can be easily integrated by adding new handlers without modifying existing code. It also helps in separating concerns by isolating the processing logic for different log types into different handler classes. This design simplifies management and scalability, enhances adaptability to new requirements and log formats, and makes the application robust against changes and expansions in log analysis needs.

## Consequences
Using the Chain of Responsibility pattern provides several advantages:
- Modularity: Each type of log is handled by a separate handler, simplifying the code structure and maintenance.
- Scalability: New handlers can be added easily as new log types are introduced, without altering existing code.
However, there are potential drawbacks:
- Performance Impact: Passing a log entry through multiple handlers may increase processing time, especially in systems with a large number of log types.
- Complexity: The chain can become complex, making debugging more challenging as the application scales.

### Instructions
To run the Java code for the log processing application, follow these steps:

1. **Install Java Development Kit (JDK):** Ensure you have Java Development Kit (JDK) installed on your system. You can download and install JDK from the official Oracle website or adopt OpenJDK distribution.

2. **Clone the Repository:** Clone the repository containing the Java code to your local machine.

3. **Navigate to Directory:** Navigate to the directory where the Java code is located.

4. **Compile Java Files:** Compile the Java files using the Java compiler (`javac`).

5. **Run Tests:** Run the test file to verify the functionality of the application.

6. **Run the Application:** If the tests pass successfully, you can run the main Java file to execute the log processing application.

7. **Check Output:** Check the output or generated files as per the application's functionality.
