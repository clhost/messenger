package messenger.net.server;


import messenger.messages.Message;
import messenger.net.protocol.ProtocolException;

import java.io.IOException;

/**
 * @author clhost
 */
public interface Session {
    void onMessage(Message msg) throws ProtocolException, IOException;
    void send(Message msg) throws ProtocolException, IOException;
}
