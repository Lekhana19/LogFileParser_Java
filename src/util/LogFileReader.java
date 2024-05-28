package util;

import handlers.Handler;
import handlers.APMHandler;
import handlers.ApplicationHandler;
import handlers.RequestHandler;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LogFileReader {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java LogFileReader --file <filename.txt>");
            System.exit(1);
        }

        String filename = args[0].replace("--file=", "");

        Handler apmHandler = new APMHandler();
        Handler appHandler = new ApplicationHandler();
        Handler reqHandler = new RequestHandler();

        apmHandler.setNext(appHandler);
        appHandler.setNext(reqHandler);

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                apmHandler.handle(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
            e.printStackTrace();
        }

        //calling aggregate
        apmHandler.final_output();
    }
}
