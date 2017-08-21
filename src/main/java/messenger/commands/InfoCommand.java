package messenger.commands;


import messenger.net.server.ClientSession;
import messenger.store.datasets.User;
import messenger.messages.InfoMessage;
import messenger.messages.Message;
import messenger.store.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InfoCommand implements Command {
    private UserService userService;
    private Logger logger = LogManager.getLogger(InfoCommand.class);

    public InfoCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void execute(ClientSession session, Message message) {
        InfoMessage infoMessage = (InfoMessage) message;
        User user = userService.getUserById(infoMessage.getUserId());

        if (user == null) {
            logger.error("Пользователь с id " + infoMessage.getUserId() + " отсутствует в базе.");
        } else {

        }
        session.send(user); // FIXME: 8/20/2017 пофиксить
    }
}
