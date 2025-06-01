package com.syos;


import com.syos.infrastructure.persistence.connection.DatabaseConnectionPool;
import com.syos.infrastructure.ui.cli.CLIApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting SYOS POS System...");

        try {
            // Initialize database connection pool
            DatabaseConnectionPool.getInstance();
            logger.info("Database connection pool initialized");

            // Start CLI application
            CLIApplication app = new CLIApplication();
            app.start();

        } catch (Exception e) {
            logger.error("Failed to start application", e);
            System.err.println("Fatal error: " + e.getMessage());
            System.exit(1);
        }
    }
}