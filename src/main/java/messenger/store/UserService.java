package messenger.store;


import messenger.store.datasets.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;

public class UserService implements UserStore {
    private static final String TABLE_MESSAGE = "MESSAGE";
    private static final String TABLE_CHAT = "CHAT";
    private static final String TABLE_USER = "USER";
    private static final String TABLE_FRIENDS = "USER_FRIEND";
    private static final String TABLE_PARTICIPANTS = "CHAT_USER";

    private static final String[] COLUMNS_TABLE_MESSAGE = {"ID", "SENDER_ID", "CHAT_ID", "TEXT", "SEND_TIME"};
    private static final String[] COLUMNS_TABLE_CHAT = {"ID", "ADMIN_ID", "CHAT_NAME"};
    private static final String[] COLUMNS_TABLE_USER = {"ID", "FIRST_NAME", "LAST_NAME", "LOGIN", "PASSWORD", "DESCRIPTION"};
    private static final String[] COLUMNS_TABLE_FRIENDS = {"USER_ID", "FRIEND_ID"};
    private static final String[] COLUMNS_TABLE_PARTICIPANTS = {"ID_CHAT", "ID_USER"};

    private static Logger logger = LogManager.getLogger(UserService.class);

    @Override
    public User addUser(String login, String password, String firstName, String lastName) {
        Connection connection = null;
        try {
            connection = ConnectionPool.getInstance().getConnection();

            try (Statement statement = connection.createStatement()) {
                ResultSet res = statement.executeQuery("SELECT max(" + COLUMNS_TABLE_USER[0] + ") AS UID FROM " + TABLE_USER + ";");
                res.next();

                int uid = res.getInt("UID");
                ++uid;

                try {
                    statement.executeUpdate("INSERT INTO " + TABLE_USER + " ( " +
                            COLUMNS_TABLE_USER[0] + ", " + COLUMNS_TABLE_USER[1] + ", " +
                            COLUMNS_TABLE_USER[2] + ", " + COLUMNS_TABLE_USER[3] + ", " +
                            COLUMNS_TABLE_USER[4] + ", " +
                            ") VALUES(" + uid + ", '" +
                            firstName + "', '" + lastName + "', '" + login + "', '" + password + "');");

                    logger.info("Пользователь " + login + " был создан.");
                } catch (SQLException e) {
                    logger.warn("Пользователь с логином " + login + " уже существует.");
                    return null;
                }

                User user = new User();
                user.setId(uid);
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setLogin(login);
                user.setPassword(password);
                return user;
            }

        } catch (SQLException e) {
            logger.error("Connection error.");
            e.printStackTrace();
        } finally {
            ConnectionPool.getInstance().putConnection(connection);
        }
        return null;
    }

    @Override
    public User updateUser(User user) {
        return null;
    }

    @Override
    public User getUser(String login, String password) {
        Connection connection = null;
        try {
            connection = ConnectionPool.getInstance().getConnection();

            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(
                        "SELECT * FROM " + TABLE_USER +
                                " WHERE " + COLUMNS_TABLE_USER[3] + "  = '" + login + "' AND " + COLUMNS_TABLE_USER[4] + " = '" + password + "';");

                while (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getInt(COLUMNS_TABLE_USER[0]));
                    user.setFirstName(resultSet.getString(COLUMNS_TABLE_USER[1]));
                    user.setLastName(resultSet.getString(COLUMNS_TABLE_USER[2]));
                    user.setLogin(resultSet.getString(COLUMNS_TABLE_USER[3]));
                    user.setPassword(resultSet.getString(COLUMNS_TABLE_USER[4]));
                    user.setDescription(resultSet.getString(COLUMNS_TABLE_USER[5]));
                    return user;
                }
            }

        } catch (SQLException e) {
            logger.error("Connection error.");
            e.printStackTrace();
        } finally {
            ConnectionPool.getInstance().putConnection(connection);
        }
        return null;
    }

    @Override
    public User getUserById(Long id) {
        Connection connection = null;
        try {
            connection = ConnectionPool.getInstance().getConnection();

            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(
                        "SELECT " + COLUMNS_TABLE_USER[0] + ", " + COLUMNS_TABLE_USER[1] + ", " + COLUMNS_TABLE_USER[2] +
                                " FROM " + TABLE_USER + " WHERE " + COLUMNS_TABLE_USER[0] + " = " + id + ";");

                while (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getInt(COLUMNS_TABLE_USER[0]));
                    user.setFirstName(resultSet.getString(COLUMNS_TABLE_USER[1]));
                    user.setLastName(resultSet.getString(COLUMNS_TABLE_USER[2]));
                    return user;
                }
            }

        } catch (SQLException e) {
            logger.error("Connection error.");
            e.printStackTrace();
        } finally {
            ConnectionPool.getInstance().putConnection(connection);
        }
        return null;
    }

    public ArrayList<Long> getUsersId(ArrayList<Long> participants) {
        String set = participants.toString().replace('[', '(').replace(']', ')');
        Connection connection = null;
        try {
            connection = ConnectionPool.getInstance().getConnection();

            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(
                        "SELECT " + COLUMNS_TABLE_USER[0] + " FROM " + TABLE_USER +
                                " WHERE " + COLUMNS_TABLE_USER[0] + " IN" + set + ";");

                ArrayList<Long> users_id = new ArrayList<>();
                while (resultSet.next()) {
                    users_id.add(resultSet.getLong(COLUMNS_TABLE_USER[0]));
                }
                return users_id;
            }

        } catch (SQLException e) {
            logger.error("Connection error.");
            e.printStackTrace();
        } finally {
            ConnectionPool.getInstance().putConnection(connection);
        }
        return null;
    }
}
