package messenger.net.protocol;

public interface Protocol {
    Object decode(byte[] bytes) throws ProtocolException;
    byte[] encode(Object data) throws ProtocolException;
}
