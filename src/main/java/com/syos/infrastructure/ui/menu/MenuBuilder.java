package com.syos.infrastructure.ui.menu;

import com.syos.infrastructure.factories.CommandFactory;

public class MenuBuilder {
    private final CommandFactory commandFactory;

    public MenuBuilder() {
        this.commandFactory = CommandFactory.getInstance();
    }

    public Menu buildMainMenu() {
        Menu mainMenu = new Menu("SYOS POS System - Main Menu");

        // Sales Menu
        Menu salesMenu = buildSalesMenu();
        mainMenu.add(new MenuItem("1", "Sales", commandFactory.createCommand("DISPLAY_SALES_MENU")));

        // Inventory Menu
        Menu inventoryMenu = buildInventoryMenu();
        mainMenu.add(new MenuItem("2", "Inventory", commandFactory.createCommand("DISPLAY_INVENTORY_MENU")));

        // Reports Menu
        Menu reportsMenu = buildReportsMenu();
        mainMenu.add(new MenuItem("3", "Reports", commandFactory.createCommand("DISPLAY_REPORTS_MENU")));

        // User Management Menu
        Menu userMenu = buildUserMenu();
        mainMenu.add(new MenuItem("4", "User Management", commandFactory.createCommand("DISPLAY_USER_MENU")));

        // Exit
        mainMenu.add(new MenuItem("0", "Exit", commandFactory.createCommand("EXIT")));

        return mainMenu;
    }

    private Menu buildSalesMenu() {
        Menu salesMenu = new Menu("Sales Menu");

        salesMenu.add(new MenuItem("1", "Create New Sale",
                commandFactory.createCommand("CREATE_SALE")));
        salesMenu.add(new MenuItem("2", "View Bills",
                commandFactory.createCommand("VIEW_BILLS")));
        salesMenu.add(new MenuItem("0", "Back to Main Menu",
                commandFactory.createCommand("BACK")));

        return salesMenu;
    }

    private Menu buildInventoryMenu() {
        Menu inventoryMenu = new Menu("Inventory Menu");

        inventoryMenu.add(new MenuItem("1", "View All Items",
                commandFactory.createCommand("VIEW_ITEMS")));
        inventoryMenu.add(new MenuItem("2", "Add Stock",
                commandFactory.createCommand("ADD_STOCK")));
        inventoryMenu.add(new MenuItem("3", "Move Items to Shelf",
                commandFactory.createCommand("MOVE_TO_SHELF")));
        inventoryMenu.add(new MenuItem("4", "Check Expiring Items",
                commandFactory.createCommand("CHECK_EXPIRING")));
        inventoryMenu.add(new MenuItem("0", "Back to Main Menu",
                commandFactory.createCommand("BACK")));

        return inventoryMenu;
    }

    private Menu buildReportsMenu() {
        Menu reportsMenu = new Menu("Reports Menu");

        reportsMenu.add(new MenuItem("1", "Daily Sales Report",
                commandFactory.createCommand("DAILY_SALES_REPORT")));
        reportsMenu.add(new MenuItem("2", "Stock Report",
                commandFactory.createCommand("STOCK_REPORT")));
        reportsMenu.add(new MenuItem("3", "Reorder Level Report",
                commandFactory.createCommand("REORDER_REPORT")));
        reportsMenu.add(new MenuItem("4", "Items to Reshelve Report",
                commandFactory.createCommand("RESHELVE_REPORT")));
        reportsMenu.add(new MenuItem("5", "Bill History",
                commandFactory.createCommand("BILL_HISTORY")));
        reportsMenu.add(new MenuItem("0", "Back to Main Menu",
                commandFactory.createCommand("BACK")));

        return reportsMenu;
    }

    private Menu buildUserMenu() {
        Menu userMenu = new Menu("User Management Menu");

        userMenu.add(new MenuItem("1", "Register New User",
                commandFactory.createCommand("REGISTER_USER")));
        userMenu.add(new MenuItem("2", "Login",
                commandFactory.createCommand("LOGIN")));
        userMenu.add(new MenuItem("3", "Logout",
                commandFactory.createCommand("LOGOUT")));
        userMenu.add(new MenuItem("0", "Back to Main Menu",
                commandFactory.createCommand("BACK")));

        return userMenu;
    }
}