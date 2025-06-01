package com.syos.infrastructure.persistence.connection;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {
    private final DatabaseConnectionPool pool;

    public ConnectionManager() {
        this.pool = DatabaseConnectionPool.getInstance();
    }

    public <T> T executeWithConnection(ConnectionCallback<T> callback) {
        Connection connection = null;
        try {
            connection = pool.acquireConnection();
            return callback.execute(connection);
        } catch (SQLException e) {
            throw new RuntimeException("Database operation failed", e);
        } finally {
            if (connection != null) {
                pool.releaseConnection(connection);
            }
        }
    }

    public void executeWithTransaction(TransactionCallback callback) {
        Connection connection = null;
        try {
            connection = pool.acquireConnection();
            connection.setAutoCommit(false);

            callback.execute(connection);

            connection.commit();
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    // Log rollback failure
                }
            }
            throw new RuntimeException("Transaction failed", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    // Log error
                }
                pool.releaseConnection(connection);
            }
        }
    }

    @FunctionalInterface
    public interface ConnectionCallback<T> {
        T execute(Connection connection) throws SQLException;
    }

    @FunctionalInterface
    public interface TransactionCallback {
        void execute(Connection connection) throws SQLException;
    }
}