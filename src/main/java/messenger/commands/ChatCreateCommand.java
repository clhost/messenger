package messenger.commands;


import messenger.store.datasets.Chat;
import messenger.messages.ChatCreateMessage;
import messenger.messages.Message;
import messenger.messages.TextMessage;
import messenger.messages.Type;
import messenger.net.ProtocolException;
import messenger.net.nonblocking.ChannelSession;
import messenger.store.MessageService;
import messenger.store.UserService;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author clhost
 */
public class ChatCreateCommand implements Command {
    private UserService userService;
    private MessageService messageService;

    public ChatCreateCommand(UserService userService, MessageService messageService) {
        this.userService = userService;
        this.messageService = messageService;
    }

    @Override
    public void execute(ChannelSession session, Message message) {
        ChatCreateMessage createMessage = (ChatCreateMessage) message;
        /* Проверка на существование пользователей : если размер списка с id из токена не равен размеру списка с id с базы данных, вернуть invalid input*/
        ArrayList<Long> input = createMessage.getParticipants();
        ArrayList<Long> output = userService.getUsersId(input); // ТУТ
        if (input.size() != output.size()) {
            input.removeAll(output);
            TextMessage textMessage = new TextMessage();
            textMessage.setType(Type.MSG_STATUS);
            textMessage.setText("Invalid input: Пользователи с id " + input.toString() + " не существуют.");
            try {
                session.send(textMessage);
            } catch (ProtocolException | IOException e) {
                e.printStackTrace();
            }
        } else {
            Chat chat = messageService.createChat(input, createMessage.getCreator_id());
            session.addChat(chat); // добавить чат в список доступных чатов пользователя
            TextMessage msg = new TextMessage();
            msg.setType(Type.MSG_CHAT_DATA);
            msg.setText(chat.getId() + ";" + chat.getAdminId() + ";" + chat.getParticipantIds().toString().replace(",", ";").replace("[", "").replace("]", "").replace(" ", ""));
            try {
                session.send(msg);
            } catch (ProtocolException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
