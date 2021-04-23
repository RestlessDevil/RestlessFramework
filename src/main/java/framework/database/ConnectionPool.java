package framework.database;

import framework.diagnostics.Monitorable;
import framework.diagnostics.Status;
import framework.diagnostics.Status.State;
import framework.settings.DatabaseSettings;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConnectionPool implements Monitorable {

    private static final ConnectionPool instance = new ConnectionPool();

    private static final Logger LOG = Logger.getLogger(ConnectionPool.class.getName());

    private final List<Connection> pool = new LinkedList<>();
    private final List<Connection> taken = new LinkedList<>();

    private final String poolLabel = "Connection Pool";
    private final boolean vital = true;
    private Status status;

    private ConnectionPool() {
        this.status = new Status(State.uninitialized, null);
    }

    public static ConnectionPool getInstance() {
        return instance;
    }

    private boolean validate(Connection jdbcConnection) {   // Make sure the connection is not killed from the server side
        try {
            PreparedStatement stmt = jdbcConnection.prepareStatement("SELECT 1;");
            stmt.executeQuery();
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public synchronized void initialize() {
        try {
            DatabaseSettings settings = DatabaseSettings.getInstance();
            if (settings.getStatus().isOperational()) {
                Class.forName(settings.getDriver());
                int poolSize = settings.getPoolSize();
                for (int i = 0; i < poolSize; i++) {
                    pool.add(DriverManager.getConnection(settings.getAddress(), settings.getUser(), settings.getPassword()));
                }
                status = new Status(State.operational, null);
            } else {
                status = new Status(State.uninitialized, null);
            }
        } catch (Exception ex) {
            status = new Status(State.malfunction, ex);
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized void shutdown() {
        try {
            for (Connection connection : pool) {
                connection.close();
            }
            pool.clear();

            for (Connection connection : taken) {
                connection.close();
            }
            taken.clear();
        } catch (Exception ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        status = new Status(State.uninitialized, null);
        notifyAll();
    }

    @Override
    public synchronized void reload() {
        shutdown();
        initialize();
        notifyAll();
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getLabel() {
        return poolLabel;
    }

    @Override
    public boolean isVital() {
        return vital;
    }

    public int getAvailable() {
        return pool.size();
    }

    public synchronized Connection getConnection() throws InterruptedException {
        while (pool.isEmpty() && status.isOperational()) {
            wait();
        }

        Connection jdbcConnection = pool.remove(0);
        taken.add(jdbcConnection);

        if (validate(jdbcConnection)) {
            return jdbcConnection;
        } else {
            try {
                jdbcConnection.close();
            } catch (Exception ex) {
            }
            try {
                DatabaseSettings settings = DatabaseSettings.getInstance();
                jdbcConnection = DriverManager.getConnection(settings.getAddress(), settings.getUser(), settings.getPassword());
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
                this.status = new Status(State.malfunction, ex);
            }
        }

        return jdbcConnection;
    }

    public synchronized void returnConnection(Connection jdbcConnection) {
        if (jdbcConnection != null) {   // A safeguard against null connections that might end up "returned"
            taken.remove(jdbcConnection);
            pool.add(jdbcConnection);
        }
        notifyAll();
    }

}
