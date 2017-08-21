package messenger.commands;


import messenger.messages.*;
import messenger.net.server.ClientSession;
import messenger.store.datasets.User;
import messenger.net.protocol.ProtocolException;
import messenger.store.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class RegCommand implements Command {
    private UserService userService;
    private Logger logger = LogManager.getLogger(RegCommand.class);


    public RegCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void execute(ClientSession session, Message message) {
        RegMessage regMessage = (RegMessage) message;
        User user = userService.addUser(regMessage.getLogin(), regMessage.getPassword(), regMessage.getFirstName(), regMessage.getLastName());

        StatusMessage status = new StatusMessage();

        if (user == null) {
            status.setStatus("Пользователь с данным логином уже существует. Пожалуйста, выберите другой.");
            session.send(status);
        } else {
            session.setUser(user); // добавить пользователя в текущую сессию
            logger.info("Зарегистрирован пользователь с id " + user.getId() + ".");
            status.setStatus("Регистрация прошла успешно. Добро пожаловать, " + user.getFirstName() + "!");

            session.send(status);
            session.send(user);
        }
    }
}
