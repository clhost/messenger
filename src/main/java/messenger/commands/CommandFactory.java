package messenger.commands;


import messenger.messages.Type;
import messenger.store.MessageService;
import messenger.store.UserService;

import java.util.HashMap;

public class CommandFactory {
    private HashMap<Type, Command> commands;

    public CommandFactory(UserService userService, MessageService messageService) {
        commands = new HashMap<>();
        commands.put(Type.MSG_TEXT, new MessageCommand(messageService));
        commands.put(Type.MSG_REG, new RegCommand(userService));
        commands.put(Type.MSG_INFO, new InfoCommand(userService));
        commands.put(Type.MSG_CHAT_CREATE, new ChatCreateCommand(userService, messageService));
        commands.put(Type.MSG_LOGIN, new AuthCommand(userService, messageService));
    }

    public Command get(Type type) {
        return commands.get(type);
    }
}
