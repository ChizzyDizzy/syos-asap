package com.syos.application.commands;

import com.syos.application.interfaces.Command;
import com.syos.infrastructure.persistence.connection.DatabaseConnectionPool;

public class ExitCommand implements Command {

    @Override
    public void execute() {
        System.out.println("Shutting down...");
        DatabaseConnectionPool.getInstance().shutdown();
        System.exit(0);
    }

    @Override
    public String getDescription() {
        return "Exit System";
    }
}