package messenger.store;


import messenger.store.datasets.User;
import java.sql.*;
import java.util.ArrayList;

public class UserService implements UserStore {
    @Override
    public User addUser(String login, String password, String firstname, String lastname) {
        try {
            Connection connection = initConnection();
            try (Statement statement = connection.createStatement()) {
                ResultSet res = statement.executeQuery("SELECT max(ID) AS UID FROM USERS;");
                res.next();
                int uid = res.getInt("uid");
                ++uid;
                try {
                    statement.executeUpdate("INSERT INTO USERS(ID, FIRSTNAME, LASTNAME, LOGIN, PASSWORD) VALUES(" + uid + ", '" +
                            firstname + "', '" + lastname + "', '" + login + "', '" + password + "');");
                    System.err.println("User " + login + " has been created.");
                } catch (SQLException e) {
                    return null; // если пользователь с таким логином уже существует
                }

                System.out.println("Created user: " + uid + " : " + firstname + " : " + lastname + " : " + login + " : " + password);
                User user = new User();
                user.setId(uid);
                user.setFirstName(firstname);
                user.setLastName(lastname);
                user.setLogin(login);
                user.setPassword(password);
                return user;
            }

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Connection error.");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User updateUser(User user) {
        return null;
    }

    @Override
    public User getUser(String login, String pass) {
        try {
            Connection connection = initConnection();
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT * FROM USERS WHERE LOGIN = '" + login + "' AND PASSWORD = '" + pass + "';");
                while (resultSet.next()) {
                    System.out.println("RETURNED FROM DB USER: " + "id: " + resultSet.getInt("id") + " | " + "Name: " + resultSet.getString("firstname")
                            + " | " + "Lastname: " + resultSet.getString("lastname")+ " | " + "Login: " + resultSet.getString("login")
                            + " | " + "Pass: " + resultSet.getString("password"));
                    User user = new User();
                    user.setId(resultSet.getInt("ID"));
                    user.setFirstName(resultSet.getString("FIRSTNAME"));
                    user.setLastName(resultSet.getString("LASTNAME"));
                    user.setLogin(resultSet.getString("LOGIN"));
                    user.setPassword(resultSet.getString("PASSWORD"));
                    return user;
                }
            }

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Connection error.");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User getUserById(Long id) {
        try {
            Connection connection = initConnection();
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT ID, FIRSTNAME, LASTNAME FROM USERS WHERE ID = " + id + ";");
                while (resultSet.next()) {
                    System.out.println(resultSet.getInt("id") + " : " + resultSet.getString("firstname")
                            + " : " + resultSet.getString("lastname"));

                    User user = new User();
                    user.setId(resultSet.getInt("ID"));
                    user.setFirstName(resultSet.getString("FIRSTNAME"));
                    user.setLastName(resultSet.getString("LASTNAME"));
                    return user;
                }
            }

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Connection error.");
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Long> getUsersId(ArrayList<Long> participants) {
        String set = participants.toString().replace('[', '(').replace(']',')');
        System.out.println(set);
        try {
            Connection connection = initConnection();
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT ID FROM USERS WHERE ID IN" + set + ";");
                ArrayList<Long> users_id = new ArrayList<>();
                while (resultSet.next()) {
                    users_id.add(resultSet.getLong("id"));
                }
                return users_id;
            }

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Connection error.");
            e.printStackTrace();
        }
        return null;
    }

    private Connection initConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        return DriverManager.getConnection("jdbc:h2:~/test", "clhost", "struct.host");
    }

    public static void main(String[] args) {
        UserService service = new UserService();
        ArrayList<Long> a = new ArrayList<>();
        a.add((long) 2);
        a.add((long) 3);
        a.add((long) 4);
        ArrayList<Long> b = service.getUsersId(a);
        System.out.println(b);
    }
}
