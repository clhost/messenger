package messenger.commands;

import messenger.messages.Message;
import messenger.messages.TextMessage;
import messenger.messages.Type;
import messenger.net.protocol.ProtocolException;
import messenger.net.server.ChannelSession;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author clhost
 */
public class ChatHistCommand implements Command {
    @Override
    public void execute(ChannelSession channelSession, Message message) {
        TextMessage chatHistMessage = (TextMessage) message;
        chatHistMessage.setType(Type.MSG_CHAT_HIST_RESULT);
        Long chatId = Long.valueOf(chatHistMessage.getText());
        chatHistMessage.setText(Arrays.toString(channelSession.getChats().get(chatId).getMessages().toArray()));
        System.err.println("RETURNED MESSAGES FROM CHAT [" + chatId + "]: " + chatHistMessage.getText());
        try {
            channelSession.send(chatHistMessage);
        } catch (ProtocolException | IOException e) {
            e.printStackTrace();
        }
    }
}
