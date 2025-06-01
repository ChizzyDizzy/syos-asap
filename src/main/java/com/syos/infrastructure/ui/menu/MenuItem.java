package com.syos.infrastructure.ui.menu;

import com.syos.application.interfaces.Command;

public class MenuItem implements MenuComponent {
    private final String name;
    private final Command command;
    private final String key;

    public MenuItem(String key, String name, Command command) {
        this.key = key;
        this.name = name;
        this.command = command;
    }

    @Override
    public void display() {
        System.out.printf("[%s] %s%n", key, name);
    }

    @Override
    public void execute() {
        command.execute();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isComposite() {
        return false;
    }

    public String getKey() {
        return key;
    }
}