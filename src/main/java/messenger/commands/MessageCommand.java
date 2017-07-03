package messenger.commands;


import messenger.messages.Message;
import messenger.messages.TextMessage;
import messenger.net.server.ChannelSession;
import messenger.net.server.NonBlockingServer;
import messenger.store.MessageService;

import java.util.List;

/**
 * @author clhost
 */
public class MessageCommand implements Command {
    private MessageService messageService;

    public MessageCommand(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void execute(ChannelSession session, Message message) {

        // сначала раскидаем по сессиям
        TextMessage textMessage = (TextMessage) message;
        List<Long> participants = session.getChatById(textMessage.getChat_id()).getParticipantIds();
        participants.add(session.getChatById(textMessage.getChat_id()).getAdminId());

        NonBlockingServer.sendMessageToOtherSessionsInRealTime(textMessage, participants);

        // затем засовываем в базу
        Long msgId = messageService.addMessage(textMessage);
        textMessage.setId(msgId);
        System.err.println("MESSAGE [" + textMessage.getText() + "] has been added into data base. Message id [" + textMessage.getId() +
                            "].  Chat id [" + textMessage.getChat_id() +
                            "]. Sender id [" + textMessage.getSenderId() + "].");
    }
}
