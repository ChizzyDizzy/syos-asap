package com.syos.infrastructure.persistence.gateways;

import com.syos.infrastructure.persistence.connection.ConnectionManager;
import java.sql.*;
import java.util.List;

public abstract class OracleDatabaseGateway<T> {
    protected final ConnectionManager connectionManager;

    protected OracleDatabaseGateway() {
        this.connectionManager = new ConnectionManager();
    }

    // Template Method Pattern
    public final T insert(T entity) {
        return connectionManager.executeWithConnection(connection -> {
            String sql = getInsertSQL();
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setInsertParameters(stmt, entity);
                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return mapResultWithId(entity, generatedKeys.getLong(1));
                    }
                }
                return entity;
            }
        });
    }

    public final void update(T entity) {
        connectionManager.executeWithConnection(connection -> {
            String sql = getUpdateSQL();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                setUpdateParameters(stmt, entity);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    public final void delete(Long id) {
        connectionManager.executeWithConnection(connection -> {
            String sql = getDeleteSQL();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, id);
                stmt.executeUpdate();
                return null;
            }
        });
    }

    public final T findById(Long id) {
        return connectionManager.executeWithConnection(connection -> {
            String sql = getFindByIdSQL();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToEntity(rs);
                    }
                    return null;
                }
            }
        });
    }

    // Template methods to be implemented by subclasses
    protected abstract String getInsertSQL();
    protected abstract String getUpdateSQL();
    protected abstract String getDeleteSQL();
    protected abstract String getFindByIdSQL();
    protected abstract void setInsertParameters(PreparedStatement stmt, T entity) throws SQLException;
    protected abstract void setUpdateParameters(PreparedStatement stmt, T entity) throws SQLException;
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;
    protected abstract T mapResultWithId(T entity, Long id);
}
