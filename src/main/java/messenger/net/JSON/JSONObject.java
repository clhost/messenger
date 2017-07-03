package messenger.net.JSON;

/**
 * @author clhost
 */
public class JSONObject {
    private String JSONObjectString;

    JSONObject() {

    }

    public JSONObject registerJSONString(String str) {
        JSONObjectString = str;
        return this;
    }

    public String getJSONString() {
        return JSONObjectString;
    }
}
