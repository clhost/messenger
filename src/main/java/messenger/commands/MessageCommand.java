package messenger.commands;


import messenger.messages.Message;
import messenger.messages.TextMessage;
import messenger.net.server.ClientSession;
import messenger.store.MessageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageCommand implements Command {
    private MessageService messageService;
    private Logger logger = LogManager.getLogger(MessageCommand.class);

    public MessageCommand(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void execute(ClientSession session, Message message) {
        TextMessage textMessage = (TextMessage) message;
        Long msgId = messageService.addMessage(textMessage);

        if (msgId != null) {
            textMessage.setId(msgId);
            logger.info("Сообщение с id " + msgId + " было добавлено в базу.");
        }
        session.addMessage(textMessage);
    }
}
