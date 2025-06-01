package com.syos.infrastructure.factories;

import com.syos.application.interfaces.Command;
import com.syos.application.commands.*;
import com.syos.application.commands.sales.*;
import com.syos.application.commands.inventory.*;
import com.syos.application.commands.reports.*;
import com.syos.application.commands.user.*;
import com.syos.infrastructure.ui.cli.InputReader;

/**
 * Factory Method Pattern - Creates commands with dependencies injected
 */
public class CommandFactory {
    private static CommandFactory instance;
    private final ServiceFactory serviceFactory;
    private final PresenterFactory presenterFactory;
    private final InputReader inputReader;

    private CommandFactory() {
        this.serviceFactory = ServiceFactory.getInstance();
        this.presenterFactory = PresenterFactory.getInstance();
        this.inputReader = InputReader.getInstance();
    }

    public static CommandFactory getInstance() {
        if (instance == null) {
            instance = new CommandFactory();
        }
        return instance;
    }

    public Command createCommand(String commandType) {
        return switch (commandType.toUpperCase()) {
            // Sales Commands
            case "CREATE_SALE" -> new CreateSaleCommand(
                    serviceFactory.getSalesService(),
                    presenterFactory.getSalesPresenter(),
                    inputReader
            );
            case "VIEW_BILLS" -> new ViewBillsCommand(
                    serviceFactory.getSalesService(),
                    presenterFactory.getSalesPresenter()
            );

            // Inventory Commands
            case "VIEW_ITEMS" -> new ViewItemsCommand(
                    serviceFactory.getInventoryService(),
                    presenterFactory.getInventoryPresenter()
            );
            case "ADD_STOCK" -> new AddStockCommand(
                    serviceFactory.getInventoryService(),
                    presenterFactory.getInventoryPresenter(),
                    inputReader
            );
            case "MOVE_TO_SHELF" -> new MoveToShelfCommand(
                    serviceFactory.getInventoryService(),
                    presenterFactory.getInventoryPresenter(),
                    inputReader
            );

            // Report Commands
            case "DAILY_SALES_REPORT" -> new DailySalesReportCommand(
                    serviceFactory.getReportService(),
                    presenterFactory.getReportPresenter(),
                    inputReader
            );
            case "STOCK_REPORT" -> new StockReportCommand(
                    serviceFactory.getReportService(),
                    serviceFactory.getInventoryService(),
                    presenterFactory.getReportPresenter(),
                    presenterFactory.getInventoryPresenter(),
                    inputReader
            );
            case "REORDER_REPORT" -> new ReorderReportCommand(
                    serviceFactory.getReportService(),
                    presenterFactory.getReportPresenter()
            );

            // User Commands
            case "REGISTER_USER" -> new RegisterUserCommand(
                    serviceFactory.getUserService(),
                    presenterFactory.getUserPresenter(),
                    inputReader
            );
            case "LOGIN" -> new LoginCommand(
                    serviceFactory.getUserService(),
                    presenterFactory.getUserPresenter(),
                    inputReader
            );
            case "LOGOUT" -> new LogoutCommand(
                    serviceFactory.getUserService(),
                    presenterFactory.getUserPresenter()
            );

            // System Commands
            case "EXIT" -> new ExitCommand();

            // Default
            default -> new NullCommand();
        };
    }
}