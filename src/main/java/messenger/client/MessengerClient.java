package messenger.client;


import messenger.store.datasets.Chat;
import messenger.store.datasets.User;
import messenger.messages.*;
import messenger.net.protocol.Protocol;
import messenger.net.protocol.ProtocolException;
import messenger.net.protocol.StringProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class MessengerClient {

    private Protocol protocol;
    private int port;
    private String host;
    private User user;
    private Socket socket;
    private List<Chat> chats;
    private Chat returnedChat;


    private InputStream in;
    private OutputStream out;

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void initSocket() throws IOException {
        socket = new Socket(host, port);
        in = socket.getInputStream();
        out = socket.getOutputStream();

        // Тред "слушает" сокет на наличие входящих сообщений от сервера
        Thread socketListenerThread = new Thread(() -> {
            final byte[] buf = new byte[1024 * 64];
            System.out.println("Starting listener thread...");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Здесь поток блокируется на ожидании данных
                    int read = in.read(buf);
                    //System.err.println("MESSAGE PREPARED");
                    if (read > 0) {

                        // По сети передается поток байт, его нужно раскодировать с помощью протокола
                        Message msg = protocol.decode(Arrays.copyOf(buf, read));
                        onMessage(msg);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to process connection: " + e);
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        });

        socketListenerThread.start();
    }

    /**
     * Реагируем на входящее сообщение
     */
    public void onMessage(Message msg) {
        Type type = msg.getType();
        switch (type) {
            case MSG_USER_DATA:
                RegMessage message = (RegMessage) msg;
                user = new User();
                user.setId(message.getId());
                user.setLogin(message.getLogin());
                user.setPassword(message.getPassword());
                user.setFirstName(message.getFirstname());
                user.setLastName(message.getLastname());
                break;
            case MSG_CHAT_DATA: //FIXME пишу тут: нет проверки на то, создан ли уже чат с данными юзерами (создать запрос и вставить ветку условия в MessageService)
                TextMessage chatDataMessage = (TextMessage) msg;
                String[] tokens = chatDataMessage.getText().split(";");
                returnedChat = new Chat();
                returnedChat.setId(Long.parseLong(tokens[1]));
                returnedChat.setAdminId(Long.parseLong(tokens[2]));

                List<Long> participants = new ArrayList<>();
                for (int i = 3; i < tokens.length; i++) {participants.add(Long.parseLong(tokens[i]));}
                returnedChat.setParticipantIds(participants);
                System.out.println("Чат с id [" + returnedChat.getId() + "] и пользователями " + returnedChat.getParticipantIds() + " был создан.");
                break;
            case MSG_STATUS:
                System.out.println(msg);
                break;
            case MSG_CHAT_LIST_RESULT:
                System.out.println("List of your chats: " + msg);
                break;
            case MSG_CHAT_HIST_RESULT:
                System.out.println("Messages:   " + msg);
                break;
            case MSG_INFO_RESULT:
                System.out.println(msg);
                break;
            default:
                //System.out.println(msg);
        }
    }

    /**
     * Обрабатывает входящую строку, полученную с консоли
     * Формат строки можно посмотреть в вики проекта
     */
    public void processInput(String line) throws IOException, ProtocolException {
        String[] tokens = line.split(" ");
        //System.out.println("Tokens: " + Arrays.toString(tokens));
        String cmdType = tokens[0];
        switch (cmdType) {
            case "/login":
                if (user == null) {
                    AuthMessage authMessage = new AuthMessage(tokens[1], tokens[2]);
                    authMessage.setType(Type.MSG_LOGIN);
                    send(authMessage);
                } else System.out.println("Invalid input: вы уже авторизованы.");
                break;
            case "/reg":
                if (user == null) {
                    RegMessage regMessage = new RegMessage(tokens[1], tokens[2]);
                    regMessage.setFullName(tokens[3], tokens[4]);
                    regMessage.setType(Type.MSG_REG);
                    send(regMessage);
                } else System.out.println("Invalid input: вы уже авторизованы.");
                break;
            case "/help":
                printHelp();
                break;
            case "/text":
                if (user != null) {
                    // собираем сообщение
                    StringBuilder messageTextBuilder = new StringBuilder();
                    for (int i = 2; i < tokens.length; i++) {
                        messageTextBuilder.append(tokens[i]).append(" ");
                    }
                    TextMessage sendMessage = new TextMessage();
                    sendMessage.setType(Type.MSG_TEXT);
                    sendMessage.setSenderId(user.getId());
                    sendMessage.setChat_id(Long.valueOf(tokens[1])); // FIXME id чата, куда можно отправить. Чат должен быть в списке чатов пользователя.
                    sendMessage.setText(messageTextBuilder.toString());
                    send(sendMessage);
                } else {
                    System.out.println("Invalid input: вы не авторизованы.");
                }
                break;
            case "/info":
                InfoMessage infoMessage = new InfoMessage();
                infoMessage.setType(Type.MSG_INFO);
                if (tokens.length == 2) {
                    infoMessage.setUserId(Long.parseLong(tokens[1]));
                } else if (user != null) {
                    infoMessage.setUserId(user.getId());
                    infoMessage.setSenderId(user.getId());
                }
                send(infoMessage);
                break;
            case "/chat_create":
                if (user != null) {
                    String[] id_tokens = tokens[1].split(",");
                    ChatCreateMessage createMessage = new ChatCreateMessage();
                    createMessage.setType(Type.MSG_CHAT_CREATE);
                    createMessage.setParticipants(id_tokens);
                    createMessage.setCreator_id(user.getId());
                    send(createMessage);
                } else {
                    System.out.println("Invalid input: вы не авторизованы.");
                }
                break;
            case "/chat_list":
                if (user != null) {
                    TextMessage chatListMessage = new TextMessage();
                    chatListMessage.setType(Type.MSG_CHAT_LIST);
                    chatListMessage.setText(String.valueOf(user.getId()));
                    chatListMessage.setSenderId(user.getId());
                    send(chatListMessage);
                } else {
                    System.out.println("Invalid input: вы не авторизованы.");
                }
                break;
            case "/chat_hist":
                if (user != null) {
                    TextMessage chatHistMessage = new TextMessage();
                    chatHistMessage.setType(Type.MSG_CHAT_HIST);
                    chatHistMessage.setText(tokens[1]);
                    chatHistMessage.setSenderId(user.getId());
                    send(chatHistMessage);
                } else {
                    System.out.println("Invalid input: вы не авторизованы.");
                }
                break;
            default:
                System.err.println("Invalid input: " + line);
        }
    }

    /**
     * Отправка сообщения в сокет клиент -> сервер
     */
    public void send(Message msg) throws IOException, ProtocolException {
        out.write(protocol.encode(msg));
        out.flush(); // принудительно проталкиваем буфер с данными
    }

    /**
     * Показывает список команд
     */
    private void printHelp() {
        PrintWriter out = new PrintWriter(System.out);
        out.println("Список команд:");
        out.println("\t /help                                                - показать список команд.");
        out.println("\t /login <логин_пользователя> <пароль>                 - залогиниться.");
        out.println("\t /reg <логин_пользователя> <пароль> <имя> <фамилия>   - зарегистрироваться.");
        out.println("\t /info <id>                                           - получить информацию о пользователе с id <id>. Без аргументов - о себе.");
        out.println("\t /chat_list                                           - получить список чатов (только для залогинненых пользователей).");
        out.println("\t /chat_create <user_id list>                          - создать чат с пользователями из <user_id list>.");
        out.println("\t /chat_hist <chat_id>                                 - вывести список сообщений из чата с id <chat_id>.");
        out.println("\t /text <id> <message>                                 - отправить сообщение <message> в чат с id <id>.");
        out.flush();
    }

    public static void main(String[] args) throws Exception {
        new MessengerClient().run();
    }

    void run() throws IOException {
        MessengerClient client = new MessengerClient();
        client.setHost("127.0.0.1");
        client.setPort(19000);
        client.setProtocol(new StringProtocol());

        try {
            client.initSocket();

            // Цикл чтения с консоли
            Scanner scanner = new Scanner(System.in);
            //System.out.print("$: ");
            while (true) {
                String input = scanner.nextLine();
                if ("q".equals(input)) {
                    return;
                }
                try {
                    client.processInput(input);
                } catch (ProtocolException | IOException e) {
                    System.err.println("Failed to process user input " + e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //System.err.println("Application failed. " + e);
        } finally {
            socket.close();
        }
    }
}