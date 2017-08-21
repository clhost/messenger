package messenger.store;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBaseInit {
    private Executor executor;

    DataBaseInit() {
        executor = new Executor();
        executor.setConnection(ConnectionPool.getInstance().getConnection());
    }

    private void initUsersTable() throws SQLException {
        executor.execUpdate("CREATE TABLE if not exists USER (\n" +
                "ID LONG,\n" +
                "FIRST_NAME VARCHAR(20) NOT NULL,\n" +
                "LAST_NAME VARCHAR(20) NOT NULL,\n" +
                "LOGIN VARCHAR(20) NOT NULL UNIQUE,\n" +
                "PASSWORD VARCHAR(20) NOT NULL,\n" +
                "DESCRIPTION VARCHAR(500), \n" +
                "PRIMARY KEY(ID)\n" +
                ");");
    }

    private void initChatTable() throws SQLException {
        executor.execUpdate("CREATE TABLE if not exists CHAT(\n" +
                "ID LONG,\n" +
                "ADMIN_ID LONG NOT NULL,\n" +
                "CHAT_NAME VARCHAR(20) NOT NULL, \n" +
                "PRIMARY KEY(ID),\n" +
                "FOREIGN KEY (ADMIN_ID) REFERENCES USER(ID)\n" +
                ");");
    }

    private void initMessageTable() throws SQLException {
        executor.execUpdate("CREATE TABLE if not exists MESSAGE (\n" +
                "ID LONG,\n" +
                "SENDER_ID LONG NOT NULL,\n" +
                "CHAT_ID LONG NOT NULL,\n" +
                "TEXT VARCHAR(500) NOT NULL,\n" +
                "SEND_TIME DATETIME NOT NULL, \n" +
                "PRIMARY KEY(ID),\n" +
                "FOREIGN KEY(SENDER_ID) REFERENCES USER(ID),\n" +
                "FOREIGN KEY(CHAT_ID) REFERENCES CHAT(ID)\n" +
                ")");
    }

    private void initChatUsersAssociateTable() throws SQLException {
        executor.execUpdate("CREATE TABLE if not exists CHAT_USER(\n" +
                "ID_CHAT LONG,\n" +
                "ID_USER LONG,\n" +
                "PRIMARY KEY(ID_CHAT, ID_USER),\n" +
                "FOREIGN KEY (ID_CHAT) REFERENCES CHAT(ID),\n" +
                "FOREIGN KEY (ID_USER) REFERENCES USER(ID)\n" +
                ");");
    }

    private void initUserFriendTable() throws SQLException {
        executor.execUpdate("CREATE TABLE if not exists USER_FRIEND (\n" +
                "  USER_ID LONG NOT NULL,\n" +
                "  FRIEND_ID LONG NOT NULL,\n" +
                "  PRIMARY KEY(USER_ID, FRIEND_ID),\n" +
                "  FOREIGN KEY (USER_ID) REFERENCES USER(ID),\n" +
                "  FOREIGN KEY (FRIEND_ID) REFERENCES USER(ID)\n" +
                ")");
    }

    public void initDataBase() throws SQLException {
        initUsersTable();
        initChatTable();
        initChatUsersAssociateTable();
        initMessageTable();
        initUserFriendTable();

        executor.close();
    }

    public static void main(String[] args) {
        try {
            new DataBaseInit().initDataBase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    class Executor {
        private Connection connection;

        public void setConnection(Connection connection) {
            this.connection = connection;
        }

        public void execUpdate(String update) throws SQLException {
            Statement stmt = connection.createStatement();
            stmt.execute(update);
            stmt.close();
        }

        public void close() {
            ConnectionPool.getInstance().putConnection(connection);
        }
    }
}
