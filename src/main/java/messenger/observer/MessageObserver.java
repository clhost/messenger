package messenger.observer;

import messenger.messages.TextMessage;

public interface MessageObserver {
    void update(Long chatid, TextMessage message);
}
