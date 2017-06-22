package messenger.commands;

import messenger.store.datasets.Chat;
import messenger.store.datasets.User;
import messenger.messages.*;
import messenger.net.ProtocolException;
import messenger.net.nonblocking.ChannelSession;
import messenger.store.MessageService;
import messenger.store.UserService;

import java.io.IOException;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author clhost
 */
public class AuthCommand implements Command {
    private UserService userService;
    private MessageService messageService;
    Logger logger = LogManager.getLogger();

    public AuthCommand(UserService userService, MessageService messageService) {
        this.userService = userService;
        this.messageService = messageService;
    }

    @Override
    public void execute(ChannelSession session, Message message) {
        AuthMessage authMessage = (AuthMessage) message;
        User user = userService.getUser(authMessage.getLogin(), authMessage.getPassword());
        TextMessage textMessage = new TextMessage();

        if (user == null) {
            textMessage.setText("Неверно введен логин или пароль или пользователь с такими данными не существует.");
        } else {
            session.setUser(user); // добавить пользователя в текущую сессию

            HashMap<Long, Chat> users = messageService.getChatsByUserId(user.getId());
            if (users != null) {
                session.setChatsToCurrentUser(users); // добавить чаты пользователя (кешируются на сессии)
            }

            textMessage.setText("Добро пожаловать, " + user.getFirstName() + "!");
        }
        textMessage.setType(Type.MSG_STATUS);

        try {
            session.send(getUserData(user));
            session.send(textMessage);
        } catch (ProtocolException | IOException e) {
            e.printStackTrace();
        }
    }

    private RegMessage getUserData(User user) throws ProtocolException, IOException { // не шифрованы
        RegMessage message = new RegMessage(user.getLogin(), user.getPassword());
        message.setType(Type.MSG_USER_DATA);
        message.setFullName(user.getFirstName(), user.getLastName());
        message.setId(user.getId());
        return message;
    }
}
