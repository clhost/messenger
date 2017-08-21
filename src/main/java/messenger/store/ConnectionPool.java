package messenger.store;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConnectionPool {
    private static ConnectionPool pool;
    private static final int CONNECTION_POOL_CAPACITY = 20;

    private BlockingQueue<Connection> connections = new ArrayBlockingQueue<>(CONNECTION_POOL_CAPACITY);
    private String url, login, password;

    public static ConnectionPool getInstance() {
        if (pool == null) {
            pool = new ConnectionPool();
        } else {
            return pool;
        }
        return pool;
    }

    private ConnectionPool() {
        try {
            FileInputStream fileInputStream = new FileInputStream("src/main/resources/config.properties");
            Properties properties = new Properties();
            properties.load(fileInputStream);

            url = properties.getProperty("db.url");
            login = properties.getProperty("db.login");
            password = properties.getProperty("db.password");

            for (int i = 0; i < CONNECTION_POOL_CAPACITY; i++) {
                connections.add(DriverManager.getConnection(url, login, password));
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            return connections.poll(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }

    public void putConnection(Connection connection) {
        if (connection == null) return;
        try {
            connections.put(connection);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
