package messenger.commands;

import messenger.messages.Message;
import messenger.net.nonblocking.ChannelSession;

/**
 * @author clhost
 */
public interface Command {
    void execute(ChannelSession channelSession, Message message);
}
