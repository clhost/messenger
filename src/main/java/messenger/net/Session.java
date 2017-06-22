package messenger.net;


import messenger.messages.Message;

import java.io.IOException;

/**
 * @author clhost
 */
public interface Session {
    void onMessage(Message msg) throws ProtocolException, IOException;
    void send(Message msg) throws ProtocolException, IOException;
}
