package messenger.commands;

import messenger.messages.Message;
import messenger.net.server.ClientSession;

public interface Command {
    void execute(ClientSession session, Message message);
}
