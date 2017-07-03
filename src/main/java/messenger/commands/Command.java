package messenger.commands;

import messenger.messages.Message;
import messenger.net.server.ChannelSession;

/**
 * @author clhost
 */
public interface Command {
    void execute(ChannelSession channelSession, Message message);
}
