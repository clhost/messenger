package messenger.store;

import messenger.store.executor.Executor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author clhost
 */
public class DataBaseInit {
    private Executor executor;

    DataBaseInit() {
        executor = new Executor(initConnection());
    }

    private void initUsersTable() throws SQLException {
        executor.execUpdate("CREATE TABLE if not exists USERS (\n" +
                "ID LONG,\n" +
                "FIRSTNAME VARCHAR(20) NOT NULL,\n" +
                "LASTNAME VARCHAR(20) NOT NULL,\n" +
                "LOGIN VARCHAR(20) NOT NULL UNIQUE,\n" +
                "PASSWORD VARCHAR(20) NOT NULL,\n" +
                "PRIMARY KEY(ID)\n" +
                ");");
    }

    private void initChatTable() throws SQLException {
        executor.execUpdate("CREATE TABLE if not exists CHAT(\n" +
                "ID LONG,\n" +
                "ADMIN_ID LONG NOT NULL,\n" +
                "PRIMARY KEY(ID),\n" +
                "FOREIGN KEY (ADMIN_ID) REFERENCES USERS(ID)\n" +
                ");");
    }

    private void initMessageTable() throws SQLException {
        executor.execUpdate("CREATE TABLE if not exists MESSAGE (\n" +
                "ID LONG,\n" +
                "SENDER_ID LONG NOT NULL,\n" +
                "CHAT_ID LONG NOT NULL,\n" +
                "MESSAGE VARCHAR(500) NOT NULL,\n" +
                "PRIMARY KEY(ID),\n" +
                "FOREIGN KEY(SENDER_ID) REFERENCES USERS(ID),\n" +
                "FOREIGN KEY(CHAT_ID) REFERENCES CHAT(ID)\n" +
                ")");
    }

    private void initChatUsersAssociateTable() throws SQLException {
        executor.execUpdate("CREATE TABLE if not exists CHATUSERS(\n" +
                "ID_CHAT LONG,\n" +
                "ID_USER LONG,\n" +
                "PRIMARY KEY(ID_CHAT, ID_USER),\n" +
                "FOREIGN KEY (ID_CHAT) REFERENCES CHAT(ID),\n" +
                "FOREIGN KEY (ID_USER) REFERENCES USERS(ID)\n" +
                ");");
    }

    private Connection initConnection() {
        try {
            Class.forName("org.h2.Driver");
            return DriverManager.getConnection("jdbc:h2:~/test", "clhost", "struct.host");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void initDataBase() throws SQLException {
        initUsersTable();
        initChatTable();
        initChatUsersAssociateTable();
        initMessageTable();
    }

    public static void main(String[] args) throws SQLException {
        new DataBaseInit().initDataBase();
    }
}
