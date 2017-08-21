package messenger.net.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import messenger.messages.*;
import messenger.store.datasets.Chat;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class JSONProtocol implements Protocol {
    private Gson gson;

    public JSONProtocol() {
        gson = new GsonBuilder().create();
    }

    public Object decode(byte[] bytes) {
        String result = new String(bytes);

        String header = result.substring(0, result.indexOf("\n"));
        String body = result.substring(result.indexOf("\n") + 1);

        if (header.equals(TextMessage.class.getSimpleName())) {
            return gson.fromJson(body, TextMessage.class);
        }

        if (header.equals(Chat.class.getSimpleName())) {
            return gson.fromJson(body, Chat.class);
        }

        if (header.equals(AuthMessage.class.getSimpleName())) {
            return gson.fromJson(body, AuthMessage.class);
        }

        if (header.equals(ChatCreateMessage.class.getSimpleName())) {
            return gson.fromJson(body, ChatCreateMessage.class);
        }

        if (header.equals(InfoMessage.class.getSimpleName())) {
            return gson.fromJson(body, InfoMessage.class);
        }

        if (header.equals(RegMessage.class.getSimpleName())) {
            return gson.fromJson(body, RegMessage.class);
        }

        return null;
    }

    public byte[] encode(Object json) {
        String header = json.getClass().getSimpleName();
        String body = gson.toJson(json);

        String builder = header + "\n" + body;
        return builder.getBytes();
    }

    public static void main(String[] args) {
        AuthMessage authMessage = new AuthMessage("admin", "admin");
        authMessage.setType(Type.MSG_LOGIN);

        JSONProtocol protocol = new JSONProtocol();

        byte[] bytes = protocol.encode(authMessage);
        Object object = protocol.decode(bytes);

        if (object instanceof AuthMessage) {
            AuthMessage message = (AuthMessage) object;
            System.out.println(message.getLogin());
        }
    }
}