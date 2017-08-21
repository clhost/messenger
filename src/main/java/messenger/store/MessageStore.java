package messenger.store;


import messenger.store.datasets.Chat;
import messenger.messages.Message;

import java.util.HashMap;
import java.util.List;

public interface MessageStore {
    /**
     * получаем чаты пользователей заданного чата
     */
    HashMap<Long, Chat> getChatsByUserId(Long userId);

    /**
     * получить информацию о чате
     */
    //Chat getChatById(Long chatId);

    /**
     * Список сообщений из чата
     */
    List<Long> getMessagesFromChat(Long chatId);

    /**
     * Получить информацию о сообщении
     */
    Message getMessageById(Long messageId);

    /**
     * Добавить сообщение в чат и вернуть его id
     */
    Long addMessage(Message message);

    /**
     * Добавить пользователя к чату
     */
    void addUserToChat(Long userId, Long chatId);

}
