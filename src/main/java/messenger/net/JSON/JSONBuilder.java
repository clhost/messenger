package messenger.net.JSON;

import messenger.messages.AuthMessage;
import messenger.messages.TextMessage;
import messenger.messages.Type;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author clhost
 * @description: Simple JSONBuilder for build simple JSON files.
 */

public class JSONBuilder {
    private StringBuilder JSONStringBuilder;
    private int count = 0; // текущее количество отступов

    JSONBuilder() {
        JSONStringBuilder = new StringBuilder().append("{\n");
    }

    public JSON build() {
        JSONStringBuilder.append("}");
        String build = JSONStringBuilder.toString();
        JSONStringBuilder.delete(0, JSONStringBuilder.length());
        return new JSON(build);
    }

    public JSONBuilder append(Object o) throws IllegalAccessException {
        Class object = o.getClass();

        Field[] objectFields = object.getDeclaredFields();
        Field[] superFields = object.getSuperclass().getDeclaredFields();

        JSONStringBuilder.append("\t\"").append(object.getSimpleName()).append("\": {\n");

        appendFields(objectFields, o, null);
        appendFields(superFields, o, "last");

        JSONStringBuilder.append("\n");
        count = 0;
        return this;
    }

    private void appendFields(Field[] fields, Object o, String param) throws IllegalAccessException {
        count++;
        for (int i = 0; i < fields.length - 1; i++) {
            fields[i].setAccessible(true);

            makeTabs(JSONStringBuilder);
            String line = "\"" + fields[i].getName() + "\": " + "\"" + fields[i].get(o) + "\",\n";
            JSONStringBuilder.append(line);
        }
        if (param != null && Objects.equals(param, "last")) {
            fields[fields.length - 1].setAccessible(true);

            makeTabs(JSONStringBuilder);
            String line = "\"" + fields[fields.length - 1].getName() + "\": " + "\"" + fields[fields.length - 1].get(o) + "\"";
            JSONStringBuilder.append(line).append("\n\t}");
        } else {
            fields[fields.length - 1].setAccessible(true);

            makeTabs(JSONStringBuilder);
            String line = "\"" + fields[fields.length - 1].getName() + "\": " + "\"" + fields[fields.length - 1].get(o) + "\",\n";
            JSONStringBuilder.append(line);
        }
        count--;
    }

    private void makeTabs(StringBuilder builder) {
        for (int i = 0; i <= count; i++) {
            builder.append("\t");
        }
    }
}