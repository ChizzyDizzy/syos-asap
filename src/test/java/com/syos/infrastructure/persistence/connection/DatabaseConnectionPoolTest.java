package com.syos.infrastructure.persistence.connection;

import com.syos.infrastructure.config.DatabaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionPoolTest {

    @Mock
    private DatabaseConfig mockDatabaseConfig;

    @Mock
    private Connection mockConnection1;

    @Mock
    private Connection mockConnection2;

    @Mock
    private Connection mockConnection3;

    @Mock
    private DatabaseMetaData mockMetaData;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Reset singleton instance before each test
        Field instanceField = DatabaseConnectionPool.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        // Setup common mock behavior for DatabaseConfig
        when(mockDatabaseConfig.getConnectionUrl()).thenReturn("jdbc:mysql://localhost:3306/syos_db");
        when(mockDatabaseConfig.getUsername()).thenReturn("testuser");
        when(mockDatabaseConfig.getPassword()).thenReturn("testpass");
        when(mockDatabaseConfig.getMaxPoolSize()).thenReturn(10);
        when(mockDatabaseConfig.getInitialPoolSize()).thenReturn(3);

        // Setup common mock behavior for connections
        when(mockConnection1.isClosed()).thenReturn(false);
        when(mockConnection2.isClosed()).thenReturn(false);
        when(mockConnection3.isClosed()).thenReturn(false);
        when(mockConnection1.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getURL()).thenReturn("jdbc:mysql://localhost:3306/syos_db");
        when(mockConnection1.getCatalog()).thenReturn("syos_db");
    }

    @Test
    void should_implement_singleton_pattern_and_initialize_pool_correctly() throws Exception {
        // Mock DatabaseConfig singleton
        try (MockedStatic<DatabaseConfig> configMock = mockStatic(DatabaseConfig.class);
             MockedStatic<DriverManager> driverMock = mockStatic(DriverManager.class);
             MockedStatic<Class> classMock = mockStatic(Class.class)) {

            configMock.when(DatabaseConfig::getInstance).thenReturn(mockDatabaseConfig);
            driverMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection1, mockConnection2, mockConnection3);

            // Mock driver loading
            Class<?> mockDriverClass = mock(Class.class);
            classMock.when(() -> Class.forName("com.mysql.cj.jdbc.Driver")).thenReturn(mockDriverClass);

            // Act - Get multiple instances
            DatabaseConnectionPool instance1 = DatabaseConnectionPool.getInstance();
            DatabaseConnectionPool instance2 = DatabaseConnectionPool.getInstance();
            DatabaseConnectionPool instance3 = DatabaseConnectionPool.getInstance();

            // Assert - Singleton behavior
            assertNotNull(instance1);
            assertSame(instance1, instance2, "getInstance() should return the same instance");
            assertSame(instance2, instance3, "getInstance() should return the same instance");

            // Verify pool initialization
            assertEquals(3, instance1.getAvailableConnectionsCount(), "Should have 3 initial connections");
            assertEquals(0, instance1.getActiveConnectionsCount(), "Should have 0 active connections initially");

            // Verify configuration was accessed
            verify(mockDatabaseConfig).getConnectionUrl();
            verify(mockDatabaseConfig).getUsername();
            verify(mockDatabaseConfig).getPassword();
            verify(mockDatabaseConfig).getMaxPoolSize();
            verify(mockDatabaseConfig).getInitialPoolSize();

            // Verify driver loading and connections created
            classMock.verify(() -> Class.forName("com.mysql.cj.jdbc.Driver"));
            driverMock.verify(() -> DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/syos_db", "testuser", "testpass"), times(4)); // 1 test + 3 initial
        }
    }

    @Test
    void should_manage_connection_acquisition_and_release_correctly() throws Exception {
        // Setup pool with mocked dependencies
        try (MockedStatic<DatabaseConfig> configMock = mockStatic(DatabaseConfig.class);
             MockedStatic<DriverManager> driverMock = mockStatic(DriverManager.class);
             MockedStatic<Class> classMock = mockStatic(Class.class)) {

            configMock.when(DatabaseConfig::getInstance).thenReturn(mockDatabaseConfig);
            driverMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection1, mockConnection2, mockConnection3);

            Class<?> mockDriverClass = mock(Class.class);
            classMock.when(() -> Class.forName("com.mysql.cj.jdbc.Driver")).thenReturn(mockDriverClass);

            DatabaseConnectionPool pool = DatabaseConnectionPool.getInstance();

            // Act - Acquire connections
            Connection conn1 = pool.acquireConnection();
            Connection conn2 = pool.acquireConnection();

            // Assert - Connection acquisition
            assertNotNull(conn1);
            assertNotNull(conn2);
            assertEquals(1, pool.getAvailableConnectionsCount(), "Should have 1 connection left in pool");
            assertEquals(0, pool.getActiveConnectionsCount(), "Active count should remain 0 for pool connections");

            // Act - Release one connection
            pool.releaseConnection(conn1);

            // Assert - Connection release
            assertEquals(2, pool.getAvailableConnectionsCount(), "Should have 2 connections after release");

            // Act - Acquire all remaining connections and one more (should create new)
            Connection conn3 = pool.acquireConnection();
            Connection conn4 = pool.acquireConnection();
            Connection conn5 = pool.acquireConnection(); // This should create a new connection

            // Assert - Pool exhaustion handling
            assertEquals(0, pool.getAvailableConnectionsCount(), "Pool should be empty");
            assertEquals(1, pool.getActiveConnectionsCount(), "Should have 1 newly created active connection");

            // Verify new connection was created
            driverMock.verify(() -> DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/syos_db", "testuser", "testpass"), times(5)); // 1 test + 3 initial + 1 new
        }
    }

    @Test
    void should_handle_pool_exhaustion_and_closed_connections_properly() throws Exception {
        // Setup pool with small max size for testing exhaustion
        when(mockDatabaseConfig.getMaxPoolSize()).thenReturn(2);
        when(mockDatabaseConfig.getInitialPoolSize()).thenReturn(1);

        try (MockedStatic<DatabaseConfig> configMock = mockStatic(DatabaseConfig.class);
             MockedStatic<DriverManager> driverMock = mockStatic(DriverManager.class);
             MockedStatic<Class> classMock = mockStatic(Class.class)) {

            configMock.when(DatabaseConfig::getInstance).thenReturn(mockDatabaseConfig);
            driverMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection1, mockConnection2, mockConnection3);

            Class<?> mockDriverClass = mock(Class.class);
            classMock.when(() -> Class.forName("com.mysql.cj.jdbc.Driver")).thenReturn(mockDriverClass);

            DatabaseConnectionPool pool = DatabaseConnectionPool.getInstance();

            // Act - Exhaust the pool
            Connection conn1 = pool.acquireConnection(); // Takes from initial pool
            Connection conn2 = pool.acquireConnection(); // Creates new connection (active count = 1)
            Connection conn3 = pool.acquireConnection(); // Creates new connection (active count = 2, reaches max)

            // Assert - Pool should be at maximum capacity
            assertEquals(0, pool.getAvailableConnectionsCount());
            assertEquals(2, pool.getActiveConnectionsCount());

            // Act & Assert - Next acquisition should fail
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                pool.acquireConnection();
            });
            assertEquals("Connection pool exhausted", exception.getMessage());

            // Test closed connection handling
            when(mockConnection1.isClosed()).thenReturn(true);

            // Release closed connection
            pool.releaseConnection(conn1);

            // Should decrement active count but not add to available pool
            assertEquals(0, pool.getAvailableConnectionsCount(), "Closed connection should not be added to pool");
            assertEquals(1, pool.getActiveConnectionsCount(), "Active count should be decremented for closed connection");

            // Test releasing valid connection
            pool.releaseConnection(conn2);
            assertEquals(1, pool.getAvailableConnectionsCount(), "Valid connection should be returned to pool");

            // Test null connection release
            assertDoesNotThrow(() -> {
                pool.releaseConnection(null);
            }, "Releasing null connection should not throw exception");

            // Test shutdown
            assertDoesNotThrow(() -> {
                pool.shutdown();
            }, "Shutdown should not throw exception");

            // Verify connections were closed during shutdown
            verify(mockConnection2, atLeastOnce()).close();
        }
    }
}