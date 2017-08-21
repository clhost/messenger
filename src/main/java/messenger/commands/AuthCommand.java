package messenger.commands;

import messenger.net.server.ClientSession;
import messenger.store.datasets.Chat;
import messenger.store.datasets.User;
import messenger.messages.*;
import messenger.store.MessageService;
import messenger.store.UserService;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class AuthCommand implements Command {
    private UserService userService;
    private MessageService messageService;
    private Logger logger = org.apache.logging.log4j.LogManager.getLogger(AuthMessage.class);


    public AuthCommand(UserService userService, MessageService messageService) {
        this.userService = userService;
        this.messageService = messageService;
    }

    @Override
    public void execute(ClientSession session, Message message) {
        AuthMessage authMessage = (AuthMessage) message;
        User user = userService.getUser(authMessage.getLogin(), authMessage.getPassword());

        StatusMessage status = new StatusMessage();
        if (user == null) {
            status.setStatus("Неверно введен логин или пароль или пользователь с такими данными не существует.");
            logger.warn("На сессии " + session + " были введены неверные аутентификационные данные.");
        } else {
            session.setUser(user);
            HashMap<Long, Chat> chats = messageService.getChatsByUserId(user.getId());
            if (chats != null) {
                session.setChats(chats); // добавить чаты пользователя (кешируются на сессии)
            }
            status.setStatus("Добро пожаловать, " + user.getFirstName() + "!");
        }
        session.send(user);
        session.send(status);
    }
}
