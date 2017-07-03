package messenger.net.protocol;

import messenger.messages.Message;

/**
 * @author clhost
 */
public class JSONProtocol implements Protocol {

    @Override
    public Message decode(byte[] bytes) throws ProtocolException {
        return null;
    }

    @Override
    public byte[] encode(Message msg) throws ProtocolException {
        return new byte[0];
    }
}
