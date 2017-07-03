package messenger.net.protocol;

import messenger.messages.Message;

/**
 * @author clhost
 */
public interface Protocol {

    Message decode(byte[] bytes) throws ProtocolException;

    byte[] encode(Message msg) throws ProtocolException;

}
