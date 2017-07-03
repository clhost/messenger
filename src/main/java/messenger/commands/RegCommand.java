package messenger.commands;


import messenger.store.datasets.User;
import messenger.messages.Message;
import messenger.messages.RegMessage;
import messenger.messages.TextMessage;
import messenger.messages.Type;
import messenger.net.protocol.ProtocolException;
import messenger.net.server.ChannelSession;
import messenger.store.UserService;

import java.io.IOException;

/**
 * @author clhost
 */
public class RegCommand implements Command {
    private UserService userService;

    public RegCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void execute(ChannelSession session, Message message) {
        RegMessage regMessage = (RegMessage) message;
        User user = userService.addUser(regMessage.getLogin(), regMessage.getPassword(), regMessage.getFirstname(), regMessage.getLastname());
        TextMessage textMessage = new TextMessage();

        if (user == null) {
            textMessage.setText("Пользователь с данным логином уже существует. Пожалуйста, выберите другой.");
        } else {
            session.setUser(user); // добавить пользователя в текущую сессию
            textMessage.setText("Регистрация прошла успешно. Добро пожаловать, " + user.getFirstName() + "!");
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
