package messenger.net.nonblocking;

import messenger.commands.Command;
import messenger.commands.CommandFactory;
import messenger.messages.Message;
import messenger.messages.TextMessage;
import messenger.net.Protocol;
import messenger.net.ProtocolException;
import messenger.net.StringProtocol;
import messenger.store.MessageService;
import messenger.store.UserService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class NonBlockingServer {

    private UserService userService;
    private MessageService messageService;
    private HashMap<SocketChannel, ByteBuffer> map;
    private static ConcurrentHashMap<Long, ChannelSession> sessions;
    private Protocol protocol;

    static {
        sessions = new ConcurrentHashMap<>();
    }

    private NonBlockingServer() {
        userService = new UserService();
        messageService = new MessageService();
        map = new HashMap<>();
        protocol = new StringProtocol();
    }

    public static void main(String[] args) {
        new NonBlockingServer().run();
    }

    private void run() {
        serverCommandsListener();
        try (ServerSocketChannel open = openAndBind()) {
            open.configureBlocking(false);
            while (true) {
                SocketChannel accept = open.accept(); //не блокируется
                if (accept != null) {
                    accept.configureBlocking(false);
                    map.put(accept, ByteBuffer.allocateDirect(4096));
                    System.err.println("Created new channel from client with remote address [" + accept.getRemoteAddress() + "]");
                }
                map.keySet().removeIf(sc -> !sc.isOpen()); // если канал закрыт
                map.forEach((socketChannel, byteBuffer) -> {
                    try {
                        int read = socketChannel.read(byteBuffer);
                        if (read == -1) {
                            for (ChannelSession session : sessions.values()) {
                                if (session.getSocketChannel().getRemoteAddress().equals(socketChannel.getRemoteAddress())) {
                                    System.err.println("Close session with id [" + session.getUser().getId() + "] and remote address: [" +
                                                        socketChannel.getRemoteAddress() + "]");
                                    closeSession(session);
                                }
                            }
                            close(socketChannel);
                        } else if (read > 0) {
                            byteBuffer.flip();

                            Message message = protocol.decode(read(byteBuffer));
                            byteBuffer.clear();

                            ChannelSession channelSession = returnSession(message.getSenderId());

                            if (channelSession == null) {
                                channelSession = new ChannelSession(socketChannel);
                            }

                            Command command = new CommandFactory(userService, messageService).get(message.getType());
                            command.execute(channelSession, message); // в соответствии с типом сообщения
                        }
                    } catch (IOException e) {
                        close(socketChannel);
                        e.printStackTrace();
                    } catch (ProtocolException e) {
                        e.printStackTrace();
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static void addSessionToSessionsPool(ChannelSession channelSession) {
        sessions.put(channelSession.getUser().getId(), channelSession);
    }

    public static void sendMessageToOtherSessionsInRealTime(TextMessage textMessage, List<Long> participants) {
        int i = 0;
        while (i < participants.size()) {
            try {
                sessions.get(participants.get(i)).getChatById(textMessage.getChad_id()).setMessage(textMessage);
            } catch (NullPointerException e) {
                // пользователь не в сети, не отсылаем ему ничего
            }
            i++;
        }
    }

    private static void closeSession(ChannelSession channelSession) {
        sessions.remove(channelSession.getUser().getId());
        channelSession.close();
    }

    private ChannelSession returnSession(Long senderId) {
        if (senderId == null) return null; // Если клиент не авторизован
        for (Long id : sessions.keySet()) {
            if (id.longValue() == senderId.longValue()) {
                return sessions.get(senderId);
            }
        }
        return null;
    }

    private static ServerSocketChannel openAndBind() throws IOException {
        ServerSocketChannel open = ServerSocketChannel.open();
        open.bind(new InetSocketAddress(19000));
        return open;
    }

    private static void close(SocketChannel sc) {
        try {
            sc.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private static byte[] read(ByteBuffer byteBuffer) {
        byte[] decoded = new byte[byteBuffer.limit()];
        for (int i = 0; i < decoded.length; i++) {
            decoded[i] = byteBuffer.get(i);
        }
        return decoded;
    }

    private void serverCommandsListener() {
        Thread commandListener = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String input = scanner.nextLine();
                switch (input) {
                    case "/print sessions":
                        printSessions(sessions);
                        break;
                    case "/print channels":
                        printChannels(map);
                        break;
                    default:
                        System.err.println("Invalid input.");
                }
            }
        });
        commandListener.start();
    }

    private void printSessions(ConcurrentHashMap<Long, ChannelSession> sessions) {
        System.out.println("------Sessions list------");
        for (Map.Entry<Long, ChannelSession> entry : sessions.entrySet()) {
            System.out.println("Key (id of user which connected): " + entry.getKey().toString() + " | Value (login): " + entry.getValue().toString());
        }
        System.out.println("-------------------------");
    }

    private void printChannels(HashMap<SocketChannel, ByteBuffer> map) {
        System.out.println("------Channels list------");
        for (Map.Entry<SocketChannel, ByteBuffer> entry : map.entrySet()) {
            System.out.println("Key : " + entry.getKey().toString() + " | Value : " + entry.getValue().toString());
        }
        System.out.println("-------------------------");
    }
}
