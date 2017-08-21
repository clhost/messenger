package messenger.observer;

import messenger.messages.TextMessage;

public interface MessageObservable {
    void registerObserver(MessageObserver observer);
    void removeObserver(MessageObserver observer);
    void notifyObservers(Long chatid, TextMessage message);
}
