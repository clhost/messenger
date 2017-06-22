package messenger.store;

import messenger.store.datasets.Chat;
import messenger.messages.Message;
import messenger.messages.TextMessage;
import messenger.messages.Type;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MessageService implements MessageStore {
    @Override
    public HashMap<Long, Chat> getChatsByUserId(Long userId) {
        class Pair {
            private Long chatId;
            private Long adminId; // или юзер id
            Pair(Long chatId, Long adminId) {
                this.chatId = chatId;
                this.adminId = adminId;
            }

            public Long getChatId() {
                return chatId;
            }

            public Long getAdminId() {
                return adminId;
            }

            @Override
            public String toString() {
                return "Chat: " + chatId + " Admin: " + adminId;
            }
        }
        try {
            Connection connection = initConnection();
            HashMap<Long, Chat> chatList = new HashMap<>();
            List<Pair> chatIds = new ArrayList<>();
            List<TextMessage> messages = new ArrayList<>();
            List<Pair> participants = new ArrayList<>();
            try (Statement statement = connection.createStatement()) {
                // определяем все чаты для юзера с id userId
                ResultSet allChats = statement.executeQuery("SELECT CHAT.ID AS CHAT_ID, ADMIN_ID FROM CHAT JOIN "  +
                                "(SELECT ID FROM CHAT WHERE ADMIN_ID =" + userId +
                                "     UNION ALL " +
                                " SELECT ID_CHAT FROM CHATUSERS WHERE ID_USER = " + userId + ") AS A ON CHAT.ID = A.ID;");
                while (allChats.next()) {
                    chatIds.add(new Pair(allChats.getLong("chat_id"), allChats.getLong("admin_id")));
                }
                System.out.println("RETURNED CHATS: " + chatIds + "   TO USER: " + userId);

                // формируем множество для следующего запроса
                StringBuilder setBuilder = new StringBuilder().append('(');
                for (int i = 0; i < chatIds.size(); i++) {
                    setBuilder.append(chatIds.get(i).getChatId()).append(',');
                }
                setBuilder.append(')');
                String set = replaceCharAt(setBuilder.toString(), setBuilder.toString().length() - 2, ' ');

                // инициализируем чаты
                for (int i = 0; i < chatIds.size(); i++) {
                    Chat chat = new Chat();
                    chat.setId(chatIds.get(i).getChatId());
                    chat.setAdminId(chatIds.get(i).getAdminId());
                    chatList.put(chatIds.get(i).getChatId(), chat);
                }

                // все сообщения для всех чатов, найденных для пользователя с id userId: второй запрос необходим, так как могут быть пустые чаты
                ResultSet resChatListSet = statement.executeQuery("SELECT * FROM MESSAGE WHERE CHAT_ID IN" + set + ";");
                Long message_id, sender_id, chat_id;
                String message;

                // формируем все сообщения
                while (resChatListSet.next()) {
                    message_id = resChatListSet.getLong("id");
                    sender_id = resChatListSet.getLong("sender_id");
                    chat_id = resChatListSet.getLong("chat_id");
                    message = resChatListSet.getString("message");

                    TextMessage textMessage = new TextMessage();
                    textMessage.setId(message_id);
                    textMessage.setSenderId(sender_id);
                    textMessage.setChad_id(chat_id);
                    textMessage.setText(message);
                    textMessage.setType(Type.MSG_TEXT);
                    messages.add(textMessage);
                }

                // помещаем во все чаты все сообщения
                for (int i = 0; i < messages.size(); i++) {
                    TextMessage textMessage = messages.get(i);
                    Long chadId = textMessage.getChad_id();
                    chatList.get(chadId).setMessage(textMessage);
                }

                // определяем всех участников всех чатов для раннее найденного юзера
                ResultSet participantsSet = statement.executeQuery("SELECT * FROM CHATUSERS WHERE ID_CHAT IN" + set + ";");
                while (participantsSet.next()) {
                    participants.add(new Pair(participantsSet.getLong("id_chat"), participantsSet.getLong("id_user")));
                }
                for (int i = 0; i < participants.size(); i++) {
                    chatList.get(participants.get(i).getChatId()).setParticipant(participants.get(i).getAdminId());
                }
            }
            return chatList;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Чатов нет нахой");
            return null;
        }
    }

    public static void main(String[] args) {
        for (Chat chat : new MessageService().getChatsByUserId(2L).values()) {
            System.out.println(Arrays.toString(chat.getMessages().toArray()));
        }
    }

    private static String replaceCharAt(String s, int pos, char c) {
        return s.substring(0,pos) + c + s.substring(pos+1);
    }

    @Override
    public List<Long> getMessagesFromChat(Long chatId) {
        return null;
    }

    @Override
    public Message getMessageById(Long messageId) {
        return null;
    }

    @Override
    public Long addMessage(Message message) {
        TextMessage txt = (TextMessage) message;
        try {
            Connection connection = initConnection();
            try (Statement statement = connection.createStatement()) {
                ResultSet res = statement.executeQuery("SELECT max(ID) AS MID FROM MESSAGE;");
                res.next();
                Long mid = res.getLong("mid");
                ++mid;
                statement.executeUpdate("INSERT INTO MESSAGE(ID, SENDER_ID, CHAT_ID, MESSAGE) VALUES('" + mid + "', '"
                                             + txt.getSenderId() + "', '" + txt.getChad_id() + "', '" + txt.getText() + "');");
                return mid;
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Connection error.");
            e.printStackTrace();
        }
        return (long) -1;
    }

    @Override
    public void addUserToChat(Long userId, Long chatId) {
        try {
            Connection connection = initConnection();
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO CHATUSERS(ID_CHAT, ID_USER) VALUES('" + chatId + "', '" + userId + "');");
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Connection error.");
            e.printStackTrace();
        }
    }

    public Chat createChat(ArrayList<Long> participants, Long admin_id) {
        try {
            Connection connection = initConnection();
            try (Statement statement = connection.createStatement()) {
                ResultSet res = statement.executeQuery("SELECT max(ID) AS CID FROM CHAT;");
                res.next();
                Long cid = res.getLong("cid");
                ++cid;
                statement.executeUpdate("INSERT INTO CHAT(ID, ADMIN_ID) VALUES('" + cid + "', '" + admin_id + "');"); // FIXME: если в базе нет чатов, вернет null и ляжет прога

                // после того, как чат создан, происходит добавление пользователей в него
                for (Long participant : participants) {
                    addUserToChat(participant, cid);
                }

                Chat chat = new Chat();
                chat.setAdminId(admin_id);
                chat.setId(cid);
                chat.setParticipantIds(participants);
                return chat;
            }

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Connection error.");
            e.printStackTrace();
        }
        return null;
    }

    private Connection initConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        return DriverManager.getConnection("jdbc:h2:~/test", "clhost", "struct.host");
    }
}
