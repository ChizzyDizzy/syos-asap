package com.syos.infrastructure.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static DatabaseConfig instance;
    private final Properties properties;

    private DatabaseConfig() {
        properties = new Properties();
        loadProperties();
    }

    public static DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config/application.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find application.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    public String getConnectionUrl() {
        return properties.getProperty("db.url", "jdbc:mysql://localhost:3306/syos_db");
    }

    public String getUsername() {
        return properties.getProperty("db.username", "root");
    }

    public String getPassword() {
        return properties.getProperty("db.password", "SportS28");
    }

    public int getMaxPoolSize() {
        return Integer.parseInt(properties.getProperty("db.pool.max", "10"));
    }

    public int getInitialPoolSize() {
        return Integer.parseInt(properties.getProperty("db.pool.initial", "5"));
    }
}