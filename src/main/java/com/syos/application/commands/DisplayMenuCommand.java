package com.syos.application.commands;

import com.syos.application.interfaces.Command;
import com.syos.infrastructure.ui.menu.Menu;

public class DisplayMenuCommand implements Command {
    private static DisplayMenuCommand instance;
    private Menu currentMenu;

    private DisplayMenuCommand() {}

    // Singleton Pattern
    public static DisplayMenuCommand getInstance() {
        if (instance == null) {
            instance = new DisplayMenuCommand();
        }
        return instance;
    }

    public void setMenu(Menu menu) {
        this.currentMenu = menu;
    }

    @Override
    public void execute() {
        if (currentMenu != null) {
            currentMenu.display();
        }
    }

    @Override
    public String getDescription() {
        return "Display Menu";
    }
}