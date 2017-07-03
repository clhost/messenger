package messenger.commands;


import messenger.store.datasets.User;
import messenger.messages.InfoMessage;
import messenger.messages.Message;
import messenger.messages.TextMessage;
import messenger.messages.Type;
import messenger.net.protocol.ProtocolException;
import messenger.net.server.ChannelSession;
import messenger.store.UserService;

import java.io.IOException;

/**
 * @author clhost
 */
public class InfoCommand implements Command {
    private UserService userService;

    public InfoCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void execute(ChannelSession session, Message message) {
        InfoMessage infoMessage = (InfoMessage) message;
        User user = userService.getUserById(infoMessage.getUserId());
        StringBuilder builder = new StringBuilder();
        String info = "Информация по пользователю с id: " + "[" + infoMessage.getUserId() + "]: ";

        if (user == null) {
            builder.append("Пользователь с указанным id не найден.");
        } else {
            builder.append(info);
            builder.append("Имя: ").append(user.getFirstName()).append(" | ");
            builder.append("Фамилия: ").append(user.getLastName());
        }

        TextMessage textMessage = new TextMessage();
        textMessage.setText(builder.toString());
        textMessage.setType(Type.MSG_INFO_RESULT);
        try {
            session.send(textMessage);
        } catch (ProtocolException | IOException e) {
            e.printStackTrace();
        }
    }
}
