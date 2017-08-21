package messenger.commands;


import messenger.net.server.ClientSession;
import messenger.store.datasets.Chat;
import messenger.messages.ChatCreateMessage;
import messenger.messages.Message;
import messenger.store.MessageService;
import messenger.store.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class ChatCreateCommand implements Command {
    private UserService userService;
    private MessageService messageService;
    private Logger logger = LogManager.getLogger(ChatCreateCommand.class);

    public ChatCreateCommand(UserService userService, MessageService messageService) {
        this.userService = userService;
        this.messageService = messageService;
    }

    @Override
    public void execute(ClientSession session, Message message) {
        ChatCreateMessage createMessage = (ChatCreateMessage) message;
        ArrayList<Long> input = createMessage.getParticipants();
        if (input != null) {
            Chat chat = messageService.createChat(input, createMessage.getCreator_id(), createMessage.getChatName());
            session.addChat(chat);
        } else {
            logger.warn("Не удалось создать чат с пользователями: " + createMessage.getParticipants().toString() + ".");
        }
    }
}
