package messenger.net.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author clhost
 */
public class JSON implements Iterable {
    private BufferedReader JSONReader;
    private BufferedReader hasNextReader;
    private StringBuilder objectBuilder;
    private String JSONString;

    JSON(String JSONString) {
        JSONReader = new BufferedReader(new StringReader(JSONString));
        hasNextReader = new BufferedReader(new StringReader(JSONString));
        objectBuilder = new StringBuilder();
        this.JSONString = JSONString;
    }

    @Override
    public Iterator iterator() {
        return new Iterator() {
            @Override
            public boolean hasNext() {
                String nextLine = null;

                while (true) {
                    try {
                        nextLine = hasNextReader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (nextLine == null) {
                        return false;
                    }
                    if (check(nextLine, "^.*\\}$")) {
                        return false;
                    }
                    if (check(nextLine, "^.*\".+\": \\{$")) {
                        // has next
                        return true;
                    }
                }
            }

            /**
             *
             * @return an JSONObject represented as a JSONString
             */
            @Override
            public Object next() {
                boolean isFound = false;
                String nextLine = null;
                objectBuilder.delete(0, objectBuilder.length());

                while (!isFound) {
                    try {
                        nextLine = JSONReader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (check(nextLine, "^.*\\}$")) {
                        isFound = true;
                    }
                    if (objectBuilder.length() != 0) {
                        objectBuilder.append(nextLine).append("\n");
                        // move for hasNext method
                        try {
                            hasNextReader.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (check(nextLine, "^.*\".+\": \\{$")) {
                        // object header was found
                        objectBuilder.append(nextLine).append("\n");
                    }
                }
                //return objectBuilder.toString();
                return new JSONObject().registerJSONString(objectBuilder.toString());
            }

            private boolean check(String line, String regExp) {
                Pattern pattern = Pattern.compile(regExp);
                Matcher matcher = pattern.matcher(line);
                return matcher.matches();
            }
        };
    }

    public String getJSONString() {
        return JSONString;
    }
}