package com.syos.infrastructure.persistence.connection;

import com.syos.infrastructure.persistence.connection.ConnectionManager.ConnectionCallback;
import com.syos.infrastructure.persistence.connection.ConnectionManager.TransactionCallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionManagerTest {

    @Mock
    private DatabaseConnectionPool mockPool;

    @Mock
    private Connection mockConnection;

    @Mock
    private ConnectionCallback<String> mockConnectionCallback;

    @Mock
    private TransactionCallback mockTransactionCallback;

    private ConnectionManager connectionManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void should_execute_callback_with_proper_connection_management() throws SQLException {
        // Arrange
        String expectedResult = "Test Result";

        // Mock the singleton and connection behavior
        try (MockedStatic<DatabaseConnectionPool> mockedStatic = mockStatic(DatabaseConnectionPool.class)) {
            mockedStatic.when(DatabaseConnectionPool::getInstance).thenReturn(mockPool);
            when(mockPool.acquireConnection()).thenReturn(mockConnection);
            when(mockConnectionCallback.execute(mockConnection)).thenReturn(expectedResult);

            connectionManager = new ConnectionManager();

            // Act
            String result = connectionManager.executeWithConnection(mockConnectionCallback);

            // Assert
            assertEquals(expectedResult, result);

            // Verify proper connection lifecycle
            verify(mockPool).acquireConnection();
            verify(mockConnectionCallback).execute(mockConnection);
            verify(mockPool).releaseConnection(mockConnection);

            // Verify connection was released exactly once
            verify(mockPool, times(1)).releaseConnection(mockConnection);
        }
    }

    @Test
    void should_handle_sql_exception_and_ensure_connection_cleanup() throws SQLException {
        // Arrange
        SQLException testException = new SQLException("Test SQL Exception");

        try (MockedStatic<DatabaseConnectionPool> mockedStatic = mockStatic(DatabaseConnectionPool.class)) {
            mockedStatic.when(DatabaseConnectionPool::getInstance).thenReturn(mockPool);
            when(mockPool.acquireConnection()).thenReturn(mockConnection);
            when(mockConnectionCallback.execute(mockConnection)).thenThrow(testException);

            connectionManager = new ConnectionManager();

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                connectionManager.executeWithConnection(mockConnectionCallback);
            });

            // Verify exception wrapping
            assertEquals("Database operation failed", thrownException.getMessage());
            assertEquals(testException, thrownException.getCause());

            // Verify proper cleanup even after exception
            verify(mockPool).acquireConnection();
            verify(mockConnectionCallback).execute(mockConnection);
            verify(mockPool).releaseConnection(mockConnection);

            // Ensure connection was still released despite exception
            verify(mockPool, times(1)).releaseConnection(mockConnection);
        }
    }

    @Test
    void should_manage_transactions_with_commit_and_rollback_scenarios() throws SQLException {
        // Test successful transaction commit
        try (MockedStatic<DatabaseConnectionPool> mockedStatic = mockStatic(DatabaseConnectionPool.class)) {
            mockedStatic.when(DatabaseConnectionPool::getInstance).thenReturn(mockPool);
            when(mockPool.acquireConnection()).thenReturn(mockConnection);

            connectionManager = new ConnectionManager();

            // Act - Successful transaction
            connectionManager.executeWithTransaction(mockTransactionCallback);

            // Assert - Verify proper transaction management
            verify(mockPool).acquireConnection();
            verify(mockConnection).setAutoCommit(false);
            verify(mockTransactionCallback).execute(mockConnection);
            verify(mockConnection).commit();
            verify(mockConnection).setAutoCommit(true);
            verify(mockPool).releaseConnection(mockConnection);
        }

        // Reset mocks for rollback test
        reset(mockPool, mockConnection, mockTransactionCallback);

        // Test transaction rollback on exception
        try (MockedStatic<DatabaseConnectionPool> mockedStatic = mockStatic(DatabaseConnectionPool.class)) {
            mockedStatic.when(DatabaseConnectionPool::getInstance).thenReturn(mockPool);
            when(mockPool.acquireConnection()).thenReturn(mockConnection);

            SQLException transactionException = new SQLException("Transaction error");
            doThrow(transactionException).when(mockTransactionCallback).execute(mockConnection);

            connectionManager = new ConnectionManager();

            // Act & Assert - Transaction with exception
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                connectionManager.executeWithTransaction(mockTransactionCallback);
            });

            // Verify exception handling
            assertEquals("Transaction failed", thrownException.getMessage());
            assertEquals(transactionException, thrownException.getCause());

            // Verify proper transaction rollback
            verify(mockPool).acquireConnection();
            verify(mockConnection).setAutoCommit(false);
            verify(mockTransactionCallback).execute(mockConnection);
            verify(mockConnection, never()).commit(); // Should not commit on exception
            verify(mockConnection).rollback(); // Should rollback
            verify(mockConnection).setAutoCommit(true); // Should restore auto-commit
            verify(mockPool).releaseConnection(mockConnection);

            // Verify rollback was called exactly once
            verify(mockConnection, times(1)).rollback();
        }

        // Test rollback exception handling (rollback itself fails)
        reset(mockPool, mockConnection, mockTransactionCallback);

        try (MockedStatic<DatabaseConnectionPool> mockedStatic = mockStatic(DatabaseConnectionPool.class)) {
            mockedStatic.when(DatabaseConnectionPool::getInstance).thenReturn(mockPool);
            when(mockPool.acquireConnection()).thenReturn(mockConnection);

            SQLException transactionException = new SQLException("Transaction error");
            SQLException rollbackException = new SQLException("Rollback error");

            doThrow(transactionException).when(mockTransactionCallback).execute(mockConnection);
            doThrow(rollbackException).when(mockConnection).rollback();

            connectionManager = new ConnectionManager();

            // Act & Assert - Transaction with rollback failure
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                connectionManager.executeWithTransaction(mockTransactionCallback);
            });

            // Verify original exception is preserved even if rollback fails
            assertEquals("Transaction failed", thrownException.getMessage());
            assertEquals(transactionException, thrownException.getCause());

            // Verify cleanup still occurs despite rollback failure
            verify(mockConnection).setAutoCommit(false);
            verify(mockConnection).rollback(); // Attempted rollback
            verify(mockConnection).setAutoCommit(true); // Still restored auto-commit
            verify(mockPool).releaseConnection(mockConnection); // Still released connection
        }
    }
}