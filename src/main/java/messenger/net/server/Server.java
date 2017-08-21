package messenger.net.server;

import messenger.messages.TextMessage;
import messenger.observer.MessageObservable;
import messenger.observer.MessageObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Server implements MessageObservable {
    private static int PORT;
    private static HashMap<SocketChannel, ClientSession> clients = new HashMap<>();
    private static List<MessageObserver> observers = new LinkedList<>();
    private Logger logger = LogManager.getLogger(Server.class);

    private Server() {
        try {
            initPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverCommandsListener();
        logger.info("Проинициализирован сервер. Порт: " + PORT + ".");
    }

    private void run() {
        try (ServerSocketChannel ssc = openChannel(); Selector selector = Selector.open()) {
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.selectNow(); // can I change to .select() ?
                for (SelectionKey key : selector.selectedKeys()) {
                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }

                    if (key.isReadable()) {
                        handleRead(key);
                    }
                }

                selector.selectedKeys().clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server().run();
    }

    private void handleRead(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        ClientSession session = clients.get(channel);
        session.read(key);
    }

    private void handleAccept(SelectionKey key) {
        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
        try {
            SocketChannel accept = channel.accept(); //non-blocking
            accept.configureBlocking(false);
            accept.register(key.selector(), SelectionKey.OP_READ);
            clients.put(accept, new ClientSession(accept, this)); // передал ссылку на Observable в Observer
            logger.info("Зарегистрирован новый клиент " + accept.getRemoteAddress() + ".");
        } catch (IOException e) {

        }
    }

    private ServerSocketChannel openChannel() throws IOException {
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.bind(new InetSocketAddress(PORT));
        channel.configureBlocking(false);
        return channel;
    }

    private void initPort() throws IOException {
        FileInputStream fileInputStream = new FileInputStream("src/main/resources/config.properties");
        Properties properties = new Properties();
        properties.load(fileInputStream);
        PORT = Integer.parseInt(properties.getProperty("server.port"));
    }

    private void serverCommandsListener() {
        Thread commandListener = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String input = scanner.nextLine();
                switch (input) {
                    case "/print clients":
                        printClients(clients);
                        break;
                    default:
                        System.err.println("Invalid input.");
                }
            }
        });
        commandListener.start();
    }

    private void printClients(HashMap<SocketChannel, ClientSession> clients) {
        System.out.println("------ Client list ------");
        for (Map.Entry<SocketChannel, ClientSession> entry : clients.entrySet()) {
            System.out.println("Key: " + entry.getKey().toString() + " | Value: " + entry.getValue().toString());
        }
        System.out.println("-------------------------");
    }

    @Override
    public void registerObserver(MessageObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(MessageObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Long chatid, TextMessage message) {
        for (MessageObserver observer : observers) {
            observer.update(chatid, message);
        }
    }
}
