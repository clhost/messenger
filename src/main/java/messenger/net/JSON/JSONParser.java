package messenger.net.JSON;

import java.io.BufferedReader;
import java.util.ArrayList;

/**
 * @author clhost
 * description: Simple JSONParser for parse simple JSON files.
 */

public class JSONParser {
    private JSON json;
    private BufferedReader JSONReader;
    private ArrayList<JSONObject> JSONArray;

    JSONParser() {
        JSONArray = new ArrayList<>();
    }

    public void registerJSON(JSON json) {
        this.json = json;
    }

    /**
     * @description: split JSON file to JSONObject's
     */
    public void parseJSON() {
        if (json != null) {
            while (json.iterator().hasNext()) {
                JSONArray.add((JSONObject) json.iterator().next());
            }
        }
    }

    public String getValue(String field) {
        boolean isFound = false;
        int index = 0;
        String line = "";
        StringBuilder builder = new StringBuilder();

        while (!isFound) {
            try {
                line = JSONReader.readLine();
                index = line.indexOf(field); // unsafe: example: "log" in "login" is always > -1
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (index != -1) {
                isFound = true;
                int count = 0;
                for (int i = line.length() - 1; i > 0; i--) {
                    if (count == 2) {
                        return builder.reverse().toString();
                    }
                    if (line.charAt(i) == '"') {
                        count++;
                    } else if (line.charAt(i) == ',') {
                        // do nothing
                    }
                    else {
                        builder.append(line.charAt(i));
                    }
                }
            }
        }
        return null;
    }
}