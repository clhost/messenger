package messenger.net.protocol;

import messenger.messages.*;

/**
 * Простейший протокол передачи данных
 */
public class StringProtocol implements Protocol {

    public static final String DELIMITER = ";";

    @Override
    public Message decode(byte[] bytes) throws ProtocolException {
        String str = new String(bytes);
        //System.err.println("decoded: " + str);
        String[] tokens = str.split(DELIMITER);
        Type type = Type.valueOf(tokens[0]);
        switch (type) {
            case MSG_TEXT:
                TextMessage textMsg = new TextMessage();
                textMsg.setSenderId(parseLong(tokens[1]));
                textMsg.setChat_id(Long.valueOf(tokens[2]));
                textMsg.setText(tokens[3]);
                textMsg.setType(type);
                return textMsg;
            case MSG_LOGIN:
                AuthMessage authMessage = new AuthMessage(tokens[1], tokens[2]);
                authMessage.setType(type);
                return authMessage;
            case MSG_REG:
                RegMessage regMessage = new RegMessage(tokens[1], tokens[2]);
                regMessage.setFullName(tokens[3], tokens[4]);
                regMessage.setType(type);
                return regMessage;
            case MSG_INFO:
                InfoMessage infoMessage = new InfoMessage();
                infoMessage.setType(type);
                infoMessage.setUserId(Long.parseLong(tokens[1]));
                if (tokens.length > 2) {
                    infoMessage.setSenderId(Long.parseLong(tokens[2]));
                }
                return infoMessage;
            case MSG_USER_DATA:
                RegMessage userDataMessage = new RegMessage(tokens[2], tokens[3]);
                userDataMessage.setId(Long.valueOf(tokens[1]));
                userDataMessage.setFullName(tokens[4], tokens[5]);
                userDataMessage.setType(type);
                return userDataMessage;
            case MSG_CHAT_CREATE:
                ChatCreateMessage createMessage = new ChatCreateMessage();
                createMessage.setCreator_id(Long.parseLong(tokens[1]));
                String[] users_id = new String[tokens.length - 2];
                System.arraycopy(tokens, 2, users_id, 0, tokens.length - 2);
                createMessage.setParticipants(users_id);
                createMessage.setType(type);
                return createMessage;
            case MSG_CHAT_DATA:
                TextMessage chatDataMessage = new TextMessage();
                chatDataMessage.setType(type);
                chatDataMessage.setText(str);
                return chatDataMessage;
            case MSG_STATUS:
                TextMessage statusMessage = new TextMessage();
                statusMessage.setType(type);
                statusMessage.setText(tokens[1]);
                return statusMessage;
            case MSG_CHAT_LIST:
                TextMessage chatListMessage = new TextMessage();
                chatListMessage.setType(type);
                chatListMessage.setText(tokens[1]);
                chatListMessage.setSenderId(Long.valueOf(tokens[2]));
                return chatListMessage;
            case MSG_CHAT_LIST_RESULT:
                TextMessage chatListResult = new TextMessage();
                chatListResult.setType(type);
                chatListResult.setText(tokens[1]);
                return chatListResult;
            case MSG_CHAT_HIST:
                TextMessage chatHist = new TextMessage();
                chatHist.setType(type);
                chatHist.setText(tokens[1]);
                chatHist.setSenderId(Long.valueOf(tokens[2]));
                return chatHist;
            case MSG_CHAT_HIST_RESULT:
                TextMessage chatHistResult = new TextMessage();
                chatHistResult.setType(type);
                chatHistResult.setText(tokens[1]);
                return chatHistResult;
            case MSG_INFO_RESULT:
                TextMessage infoResultMessage = new TextMessage();
                infoResultMessage.setType(type);
                infoResultMessage.setText(tokens[1]);
                return infoResultMessage;
            default:
                throw new ProtocolException("Invalid type: " + type);
        }
    }

    @Override
    public byte[] encode(Message msg) throws ProtocolException {
        StringBuilder builder = new StringBuilder();
        Type type = msg.getType();
        builder.append(type).append(DELIMITER);
        switch (type) {
            case MSG_TEXT:
                TextMessage sendMessage = (TextMessage) msg;
                builder.append(String.valueOf(sendMessage.getSenderId())).append(DELIMITER);
                builder.append(sendMessage.getChat_id()).append(DELIMITER);
                builder.append(sendMessage.getText()).append(DELIMITER);
                break;
            case MSG_LOGIN:
                AuthMessage authMessage = (AuthMessage) msg;
                builder.append(authMessage.getLogin()).append(DELIMITER);
                builder.append(authMessage.getPassword()).append(DELIMITER);
                break;
            case MSG_REG:
                RegMessage regMessage = (RegMessage) msg;
                builder.append(regMessage.getLogin()).append(DELIMITER);
                builder.append(regMessage.getPassword()).append(DELIMITER);
                builder.append(regMessage.getFirstname()).append(DELIMITER);
                builder.append(regMessage.getLastname()).append(DELIMITER);
                break;
            case MSG_INFO:
                InfoMessage infoMessage = (InfoMessage) msg;
                builder.append(infoMessage.getUserId()).append(DELIMITER);
                Long senderId = infoMessage.getSenderId();
                if (senderId != null) {
                    builder.append(infoMessage.getSenderId());
                }
                break;
            case MSG_USER_DATA: // от сервера
                RegMessage userDataMessage = (RegMessage) msg;
                builder.append(userDataMessage.getId()).append(DELIMITER);
                builder.append(userDataMessage.getLogin()).append(DELIMITER);
                builder.append(userDataMessage.getPassword()).append(DELIMITER);
                builder.append(userDataMessage.getFirstname()).append(DELIMITER);
                builder.append(userDataMessage.getLastname()).append(DELIMITER);
                break;
            case MSG_CHAT_CREATE:
                ChatCreateMessage createMessage = (ChatCreateMessage) msg;
                builder.append(createMessage.getCreator_id()).append(DELIMITER);
                for (Long l : createMessage.getParticipants()) {
                    builder.append(l).append(DELIMITER);
                }
                break;
            case MSG_CHAT_DATA:
                TextMessage textMessage = (TextMessage) msg;
                builder.append(textMessage.getText());
                break;
            case MSG_STATUS:
                TextMessage statusMessage = (TextMessage) msg;
                builder.append(statusMessage.getText());
                break;
            case MSG_CHAT_LIST:
                TextMessage chatListMessage = (TextMessage) msg;
                builder.append(chatListMessage.getText()).append(DELIMITER);
                builder.append(chatListMessage.getSenderId());
                break;
            case MSG_CHAT_LIST_RESULT:
                TextMessage chatListResult = (TextMessage) msg;
                builder.append(chatListResult.getText());
                break;
            case MSG_CHAT_HIST:
                TextMessage chatHistMessage = (TextMessage) msg;
                builder.append(chatHistMessage.getText()).append(DELIMITER);
                builder.append(chatHistMessage.getSenderId());
                break;
            case MSG_CHAT_HIST_RESULT: //FIXME in future
                TextMessage chatHistResult = (TextMessage) msg;
                builder.append(chatHistResult.getText());
                break;
            case MSG_INFO_RESULT:
                TextMessage infoResultMessage = (TextMessage) msg;
                builder.append(infoResultMessage.getText());
                break;
            default:
                throw new ProtocolException("Invalid type: " + type);
        }
        //System.out.println("encoded: " + builder);
        return builder.toString().getBytes();
    }

    private Long parseLong(String str) {
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
            // who care
        }
        return null;
    }
}