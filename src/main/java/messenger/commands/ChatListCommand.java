package messenger.commands;

import messenger.messages.Message;
import messenger.net.nonblocking.ChannelSession;

/**
 * @author clhost
 */
public class ChatListCommand implements Command {

    public ChatListCommand() {}

    @Override
    public void execute(ChannelSession session, Message message) {
        session.returnChatList();
    }
}
