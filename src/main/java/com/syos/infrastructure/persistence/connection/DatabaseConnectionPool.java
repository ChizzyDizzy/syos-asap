package com.syos.infrastructure.persistence.connection;

import com.syos.infrastructure.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseConnectionPool {
    private static DatabaseConnectionPool instance;
    private final ConcurrentLinkedQueue<Connection> availableConnections;
    private final AtomicInteger activeConnections;
    private final DatabaseConfig config;
    private final int maxPoolSize;

    private DatabaseConnectionPool() {
        this.config = DatabaseConfig.getInstance();
        this.maxPoolSize = config.getMaxPoolSize();
        this.availableConnections = new ConcurrentLinkedQueue<>();
        this.activeConnections = new AtomicInteger(0);

        try {
            initializePool();
        } catch (Exception e) {
            System.err.println("=".repeat(60));
            System.err.println("DATABASE CONNECTION ERROR");
            System.err.println("=".repeat(60));
            System.err.println("Failed to initialize database connection pool");
            System.err.println("Error: " + e.getMessage());
            System.err.println("\nPossible causes:");
            System.err.println("1. MySQL is not running");
            System.err.println("2. Wrong username/password in application.properties");
            System.err.println("3. Database 'syos_db' does not exist");
            System.err.println("4. MySQL port is not 3306");
            System.err.println("\nDebug Information:");
            System.err.println("URL: " + config.getConnectionUrl());
            System.err.println("Username: " + config.getUsername());
            System.err.println("=".repeat(60));
            throw new RuntimeException("Failed to create database connection", e);
        }
    }

    public static synchronized DatabaseConnectionPool getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionPool();
        }
        return instance;
    }

    private void initializePool() {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL driver loaded successfully");

            // Test connection first
            testConnection();

            // Create initial connections
            int initialSize = config.getInitialPoolSize();
            System.out.println("Creating " + initialSize + " initial connections...");

            for (int i = 0; i < initialSize; i++) {
                Connection conn = createConnection();
                availableConnections.offer(conn);
                System.out.println("Connection " + (i + 1) + " created");
            }

            System.out.println("Database connection pool initialized successfully");

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found. Make sure mysql-connector-java is in your classpath", e);
        }
    }

    private void testConnection() {
        System.out.println("Testing database connection...");
        try (Connection conn = DriverManager.getConnection(
                config.getConnectionUrl(),
                config.getUsername(),
                config.getPassword())) {

            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Database connection successful!");
                System.out.println("Connected to: " + conn.getMetaData().getURL());
                System.out.println("Database: " + conn.getCatalog());
            }
        } catch (SQLException e) {
            System.err.println("✗ Database connection failed!");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());

            if (e.getMessage().contains("Unknown database")) {
                System.err.println("\nThe database 'syos_db' does not exist.");
                System.err.println("Please create it using: CREATE DATABASE syos_db;");
            } else if (e.getMessage().contains("Access denied")) {
                System.err.println("\nAccess denied. Check your username and password.");
            } else if (e.getMessage().contains("Communications link failure")) {
                System.err.println("\nMySQL server is not running or not accessible.");
                System.err.println("Make sure MySQL is running on port 3306.");
            }

            throw new RuntimeException("Database connection test failed", e);
        }
    }

    private Connection createConnection() {
        try {
            Connection conn = DriverManager.getConnection(
                    config.getConnectionUrl(),
                    config.getUsername(),
                    config.getPassword()
            );

            // Set connection properties
            conn.setAutoCommit(true);

            return conn;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create database connection", e);
        }
    }

    public Connection acquireConnection() {
        Connection connection = availableConnections.poll();

        if (connection == null) {
            if (activeConnections.get() < maxPoolSize) {
                connection = createConnection();
                activeConnections.incrementAndGet();
            } else {
                throw new RuntimeException("Connection pool exhausted");
            }
        } else {
            try {
                if (connection.isClosed()) {
                    connection = createConnection();
                }
            } catch (SQLException e) {
                connection = createConnection();
            }
        }

        return connection;
    }

    public void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    availableConnections.offer(connection);
                } else {
                    activeConnections.decrementAndGet();
                }
            } catch (SQLException e) {
                activeConnections.decrementAndGet();
            }
        }
    }

    public void shutdown() {
        System.out.println("Shutting down connection pool...");
        Connection connection;
        while ((connection = availableConnections.poll()) != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Log error
            }
        }
        System.out.println("Connection pool shut down successfully");
    }

    public int getAvailableConnectionsCount() {
        return availableConnections.size();
    }

    public int getActiveConnectionsCount() {
        return activeConnections.get();
    }
}