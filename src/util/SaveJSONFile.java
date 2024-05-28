package util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class SaveJSONFile {
    public static void writeMapToJson(Map map, String filePath)  {
        JSONObject jsonObject = new JSONObject(map);
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(jsonObject.toString(4)); // 4 is for indentation level
        }
        catch (IOException | JSONException e) {
            System.out.println("Something went wrong when trying to write the map to the file.");
        }
    }
}
