package messenger.store;

import messenger.store.datasets.Chat;
import messenger.messages.Message;
import messenger.messages.TextMessage;
import messenger.messages.Type;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class MessageService implements MessageStore {

    private static final String TABLE_MESSAGE = "MESSAGE";
    private static final String TABLE_CHAT = "CHAT";
    private static final String TABLE_PARTICIPANTS = "CHAT_USER";

    private static final String[] COLUMNS_TABLE_MESSAGE = {"ID", "SENDER_ID", "CHAT_ID", "TEXT", "SEND_TIME"};
    private static final String[] COLUMNS_TABLE_CHAT = {"ID", "ADMIN_ID", "CHAT_NAME"};
    private static final String[] COLUMNS_TABLE_PARTICIPANTS = {"ID_CHAT", "ID_USER"};

    private static Logger logger = LogManager.getLogger(MessageService.class);

    @Override
    public HashMap<Long, Chat> getChatsByUserId(Long userId) { // TODO: 8/18/2017 заново пересмотреть запрос
        class Pair {
            private Long chatId;
            private Long adminId; // или юзер id

            private Pair(Long chatId, Long adminId) {
                this.chatId = chatId;
                this.adminId = adminId;
            }

            private Long getChatId() {
                return chatId;
            }

            private Long getAdminId() {
                return adminId;
            }

            @Override
            public String toString() {
                return "Chat: " + chatId + " Admin: " + adminId;
            }
        }

        Connection connection = null;
        try {
            connection = ConnectionPool.getInstance().getConnection();

            HashMap<Long, Chat> chatList = new HashMap<>();
            List<Pair> chatIds = new ArrayList<>();
            List<TextMessage> messages = new ArrayList<>();
            List<Pair> participants = new ArrayList<>();

            try (Statement statement = connection.createStatement()) {
                // определяем все чаты для юзера с id userId
                ResultSet allChats = statement.executeQuery(
                        "SELECT " + TABLE_CHAT + "." + COLUMNS_TABLE_CHAT[0] + " AS CHAT_ID, " + COLUMNS_TABLE_CHAT[1] + " FROM " + TABLE_CHAT + " JOIN " +
                                "(SELECT " + COLUMNS_TABLE_CHAT[0] + " FROM " + TABLE_CHAT +
                                " WHERE " + COLUMNS_TABLE_CHAT[1] + " = " + userId +
                                "     UNION ALL " +
                                " SELECT " + COLUMNS_TABLE_PARTICIPANTS[0] + " FROM " + TABLE_PARTICIPANTS +
                                " WHERE " + COLUMNS_TABLE_PARTICIPANTS[1] + " = " + userId + ") AS A ON " +
                                TABLE_CHAT + "." + COLUMNS_TABLE_CHAT[0] + " = A." + COLUMNS_TABLE_CHAT[0] + ";");

                while (allChats.next()) {
                    chatIds.add(new Pair(allChats.getLong("CHAT_ID"), allChats.getLong(COLUMNS_TABLE_CHAT[1])));
                }

                // формируем множество для следующего запроса
                StringBuilder setBuilder = new StringBuilder().append('(');
                for (Pair chatId : chatIds) {
                    setBuilder.append(chatId.getChatId()).append(',');
                }
                setBuilder.append(')');
                String set = replaceCharAt(setBuilder.toString(), setBuilder.toString().length() - 2, ' ');

                // инициализируем чаты
                for (Pair chatId : chatIds) {
                    Chat chat = new Chat();
                    chat.setId(chatId.getChatId());
                    chat.setAdminId(chatId.getAdminId());
                    chatList.put(chatId.getChatId(), chat);
                }

                // все сообщения для всех чатов, найденных для пользователя с id userId: второй запрос необходим, так как могут быть пустые чаты
                ResultSet resChatListSet = statement.executeQuery(
                        "SELECT * FROM " + TABLE_MESSAGE +
                                " WHERE " + COLUMNS_TABLE_MESSAGE[2] + " IN" + set + ";");

                Long message_id, sender_id, chat_id;
                String message;
                // формируем все сообщения
                while (resChatListSet.next()) {
                    message_id = resChatListSet.getLong(COLUMNS_TABLE_MESSAGE[0]);
                    sender_id = resChatListSet.getLong(COLUMNS_TABLE_MESSAGE[1]);
                    chat_id = resChatListSet.getLong(COLUMNS_TABLE_MESSAGE[2]);
                    message = resChatListSet.getString(COLUMNS_TABLE_MESSAGE[3]); // FIXME: 8/18/2017 send time его нет

                    TextMessage textMessage = new TextMessage();
                    textMessage.setId(message_id);
                    textMessage.setSenderId(sender_id);
                    textMessage.setChat_id(chat_id);
                    textMessage.setText(message);
                    textMessage.setType(Type.MSG_TEXT);

                    messages.add(textMessage);
                }

                // помещаем во все чаты все сообщения
                for (TextMessage textMessage : messages) {
                    Long chadId = textMessage.getChat_id();
                    chatList.get(chadId).addMessage(textMessage);
                }

                // определяем всех участников всех чатов для раннее найденного юзера
                ResultSet participantsSet = statement.executeQuery(
                        "SELECT * FROM " + TABLE_PARTICIPANTS +
                                " WHERE " + COLUMNS_TABLE_PARTICIPANTS[0] + " IN" + set + ";");

                while (participantsSet.next()) {
                    participants.add(new Pair(participantsSet.getLong(COLUMNS_TABLE_PARTICIPANTS[0]), participantsSet.getLong(COLUMNS_TABLE_PARTICIPANTS[1])));
                }
                for (Pair participant : participants) {
                    chatList.get(participant.getChatId()).setParticipant(participant.getAdminId());
                }
            }
            return chatList;
        } catch (SQLException e) {
            logger.error("Connection error.");
            return null;
        } finally {
            ConnectionPool.getInstance().putConnection(connection);
        }
    }

    @SuppressWarnings("all")
    private static String replaceCharAt(String s, int pos, char c) {
        return s.substring(0, pos) + c + s.substring(pos + 1);
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
        Connection connection = null;
        try {
            connection = ConnectionPool.getInstance().getConnection();

            try (Statement statement = connection.createStatement()) {
                ResultSet res = statement.executeQuery("SELECT max(" + COLUMNS_TABLE_MESSAGE[0] + ") AS MID FROM " + TABLE_MESSAGE + ";");
                res.next();

                Long mid = res.getLong("MID");
                ++mid;

                Date date = Calendar.getInstance().getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                statement.executeUpdate(
                        "INSERT INTO " + TABLE_MESSAGE + "(" + COLUMNS_TABLE_MESSAGE[0] + ", " + COLUMNS_TABLE_MESSAGE[1] + ", " + COLUMNS_TABLE_MESSAGE[2] +
                                ", " + COLUMNS_TABLE_MESSAGE[3] + ", " + COLUMNS_TABLE_MESSAGE[4] + ") VALUES('" +
                                mid + "', '" + txt.getSenderId() + "', '" + txt.getChat_id() + "', '" + txt.getText() + "', '" + dateFormat.format(date) + "');");
                return mid;
            }
        } catch (SQLException e) {
            logger.error("Connection error.");
            e.printStackTrace();
        } finally {
            ConnectionPool.getInstance().putConnection(connection);
        }
        return (long) -1;
    }

    @Override
    public void addUserToChat(Long userId, Long chatId) {
        Connection connection = null;
        try {
            connection = ConnectionPool.getInstance().getConnection();
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                        "INSERT INTO " + TABLE_PARTICIPANTS + " (" + COLUMNS_TABLE_PARTICIPANTS[0] + ", " + COLUMNS_TABLE_PARTICIPANTS[1] +
                                ") VALUES('" + chatId + "', '" + userId + "');");
            }
        } catch (SQLException e) {
            logger.error("Connection error.");
            e.printStackTrace();
        } finally {
            ConnectionPool.getInstance().putConnection(connection);
        }
    }

    public Chat createChat(ArrayList<Long> participants, Long admin_id, String chatName) {
        Connection connection = null;
        try {
            connection = ConnectionPool.getInstance().getConnection();

            try (Statement statement = connection.createStatement()) {
                ResultSet res = statement.executeQuery("SELECT max(" + COLUMNS_TABLE_CHAT[0] + ") AS CID FROM " + TABLE_CHAT + ";");
                res.next();

                Long cid = res.getLong("cid");
                ++cid;

                statement.executeUpdate(
                        "INSERT INTO " + TABLE_CHAT + "(" + COLUMNS_TABLE_CHAT[0] + ", " + COLUMNS_TABLE_CHAT[1] + ", " + COLUMNS_TABLE_CHAT[2] +
                                ") VALUES('" + cid + "', '" + admin_id + ", " + chatName + "');");

                // после того, как чат создан, происходит добавление пользователей в него
                for (Long participant : participants) {
                    addUserToChat(participant, cid);
                }

                Chat chat = new Chat();
                chat.setAdminId(admin_id);
                chat.setId(cid);
                chat.setParticipantIds(participants);
                chat.setChatName(chatName);
                return chat;
            }

        } catch (SQLException e) {
            logger.error("Connection error.");
            e.printStackTrace();
        } finally {
            ConnectionPool.getInstance().putConnection(connection);
        }
        return null;
    }
}