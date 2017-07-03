package messenger.net.server;


import messenger.store.datasets.Chat;
import messenger.store.datasets.User;
import messenger.messages.Message;
import messenger.messages.TextMessage;
import messenger.messages.Type;
import messenger.net.protocol.Protocol;
import messenger.net.protocol.ProtocolException;
import messenger.net.protocol.StringProtocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

/**
 * @author clhost
 */
public class ChannelSession implements Session {

    private User user;
    private Protocol protocol;
    private SocketChannel socketChannel;
    private HashMap<Long, Chat> chats;

    public ChannelSession(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        protocol = new StringProtocol();
        chats = new HashMap<>();
    }

    public void setUser(User user) {
        this.user = user;
        NonBlockingServer.addSessionToSessionsPool(this);
    }
    public User getUser() {
        return user;
    }

    public HashMap<Long, Chat> getChats() {
        return chats;
    }
    public Chat getChatById(Long id) {
        return chats.get(id);
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setChatsToCurrentUser(HashMap<Long, Chat> chats) {
        this.chats = chats;
    }

    public void returnChatList() {
        TextMessage chatListResult = new TextMessage();
        chatListResult.setType(Type.MSG_CHAT_LIST_RESULT);
        chatListResult.setText(chats.keySet().toString());
        try {
            send(chatListResult);
        } catch (ProtocolException | IOException e) {
            e.printStackTrace();
        }
    }

    public void addChat(Chat chat) {
        chats.put(chat.getId(), chat);
    }

    @Override
    public void onMessage(Message msg) {

    }

    @Override
    public void send(Message msg) throws ProtocolException, IOException {
        byte[] encoded = protocol.encode(msg);
        ByteBuffer buf = ByteBuffer.allocate(4096);

        buf.clear();
        buf.put(encoded);
        buf.flip();

        socketChannel.write(buf);
        buf.compact();
    }

    @Override
    public String toString() {
        try {
            return user.getLogin() + " | Channel session's remote address: [" + socketChannel.getRemoteAddress() + "]";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return user.getLogin() + " n/a";
    }

    // Сборщик подметет
    public void close() {
        user = null;
        protocol = null;
        chats = null;
        socketChannel = null;
    }
}
