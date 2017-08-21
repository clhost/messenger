package messenger.net.server;

import messenger.commands.Command;
import messenger.commands.CommandFactory;
import messenger.messages.*;
import messenger.net.protocol.JSONProtocol;
import messenger.net.protocol.Protocol;
import messenger.net.protocol.ProtocolException;
import messenger.observer.MessageObserver;
import messenger.store.MessageService;
import messenger.store.UserService;
import messenger.store.datasets.Chat;
import messenger.store.datasets.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;

public class ClientSession implements MessageObserver {
    private ByteBuffer buffer;
    private SocketChannel channel;
    private ClientSessionType sessionType = ClientSessionType.EMPTY_SESSION;
    private Protocol protocol = new JSONProtocol();
    private UserService userService = new UserService();
    private MessageService messageService = new MessageService();
    private User user;
    private HashMap<Long, Chat> chats;
    private Server server;
    private Logger logger = LogManager.getLogger(ClientSession.class);

    ClientSession(SocketChannel channel, Server server) throws IOException {
        this.channel = (SocketChannel) channel.configureBlocking(false);
        this.server = server;
        server.registerObserver(this);
        buffer = ByteBuffer.allocateDirect(1024);
        logger.info("Была создана пустая сессия: " + this.toString());
    }

    public void read(SelectionKey key) {
        try {
            int read = channel.read(buffer);
            if (read == -1) {
                channel.close();
            } else if (read > 0) {
                buffer.flip();
                key.interestOps(SelectionKey.OP_WRITE);

                processData(read(buffer));
            }
        } catch (IOException | ProtocolException e) {
            e.printStackTrace();
        }
    }

    private static byte[] read(ByteBuffer byteBuffer) {
        byte[] decoded = new byte[byteBuffer.limit()];
        for (int i = 0; i < decoded.length; i++) {
            decoded[i] = byteBuffer.get(i);
        }
        return decoded;
    }

    public void send(Object object) {
        try {
            byte[] encoded = protocol.encode(object);

            buffer.clear();
            buffer.put(encoded);
            buffer.flip();

            channel.write(buffer);
            buffer.compact();

        } catch (IOException | ProtocolException e) {
            e.printStackTrace();
        }
    }

    public void setUser(User user) {
        if (this.user == null) {
            this.user = user;
            sessionType = ClientSessionType.USER_SESSION;
            logger.info("Сессия " + this.toString() + " стала пользовательской. Пользователь " + user.getLogin() + " был успешно добавлен.");
        } else {
            logger.warn("Сессия " + this.toString() + " уже является пользовательской.");
        }
    }

    public void setChats(HashMap<Long, Chat> chats) {
        if (sessionType == ClientSessionType.USER_SESSION) {
            this.chats = chats;
            logger.info("На сессию " + this.toString() + " были добавлены чаты.");
        } else if (sessionType == ClientSessionType.EMPTY_SESSION) {
            logger.warn("Сессия " + this.toString() + " не является пользовательской.");
        }
    }

    public void addChat(Chat chat) {
        if (chats == null) {
            chats = new HashMap<>();
        }
        chats.put(chat.getId(), chat);
        logger.info("Чат с id: " + chat.getId() + " был добавлен в список чатов на сессии " + this.toString() + ".");
    }

    public void addMessage(TextMessage message) {
        if (message != null) {
            long chatid = message.getChat_id();
            server.notifyObservers(chatid, message); // произошли изменения в Observer -> оповещаем Server -> сервер оповещает всех
            logger.info("Сессия " + this.toString() + " оповещает остальных.");
        }
    }

    private void processData(byte[] data) throws ProtocolException {
        Object decoded = protocol.decode(data);
        Message message = null;

        if (decoded instanceof AuthMessage) {
            message = (AuthMessage) decoded;
        }

        if (decoded instanceof RegMessage) {
            message = (RegMessage) decoded;
        }

        if (decoded instanceof ChatCreateMessage) {
            message = (ChatCreateMessage) decoded;
        }

        if (decoded instanceof TextMessage) {
            message = (TextMessage) decoded;
        }

        if (decoded instanceof InfoMessage) {
            message = (InfoMessage) decoded;
        }

        if (message != null) {
            logger.info("Обработка входных данных. Тип: " + message.getClass() + ". Сессия: " + this.toString() + ".");
            Command command = new CommandFactory(userService, messageService).get(message.getType());
            command.execute(this, message);
        }
    }

    public void disconnect() {
        if (channel == null) return;
        try {
            server.removeObserver(this);
            channel.close();
            buffer.clear();
            chats.clear();
            user = null;
            userService = null;
            messageService = null;
            chats = null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            logger.info("Сессия " + this.toString() + " была завершена.");
        }
    }

    @Override
    public String toString() {
        String str = null;
        try {
            str = channel.getRemoteAddress().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    @Override
    public void update(Long chatid, TextMessage message) {
        Chat chat = chats.get(chatid);
        if (chat != null) {
            chats.get(chatid).addMessage(message);
            send(message);
            logger.info("На сессии " + this.toString() + " было добавлено новое сообщение в чат id: " +
                        chatid + " и разослано остальным клиентам.");
        }
    }

    enum ClientSessionType {
        EMPTY_SESSION,
        USER_SESSION
    }
}
