package messenger.store.executor;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author clhost
 */
public class Executor {
    private final Connection connection;

    public Executor(Connection connection) {
        this.connection = connection;
    }

    public void execUpdate(String update) throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute(update);
        stmt.close();
    }
}
