package com.syos.infrastructure.ui.cli;

import com.syos.infrastructure.ui.menu.*;
import com.syos.application.interfaces.Command;
import com.syos.infrastructure.factories.CommandFactory;
import java.util.Scanner;

public class CLIApplication {
    private final Scanner scanner;
    private final MenuBuilder menuBuilder;
    private final CommandFactory commandFactory;
    private boolean running;

    public CLIApplication() {
        this.scanner = new Scanner(System.in);
        this.menuBuilder = new MenuBuilder();
        this.commandFactory = CommandFactory.getInstance();
        this.running = true;
    }

    public void start() {
        showWelcomeMessage();

        while (running) {
            try {
                Menu mainMenu = menuBuilder.buildMainMenu();
                mainMenu.display();

                String choice = scanner.nextLine().trim().toUpperCase();
                processMenuChoice(mainMenu, choice);

            } catch (Exception e) {
                System.err.println("Error:asdads " + e.getMessage());
            }
        }

        showGoodbyeMessage();
    }

    private void processMenuChoice(Menu menu, String choice) {
        MenuComponent component = menu.getComponent(choice);

        if (component != null) {
            component.execute();
        } else {
            Command command = commandFactory.createCommand(choice);
            command.execute();
        }
    }

    private void showWelcomeMessage() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Welcome to SYOS POS System");
        System.out.println("Version 1.0.0");
        System.out.println("=".repeat(60) + "\n");
    }

    private void showGoodbyeMessage() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Thank you for using SYOS POS System");
        System.out.println("Goodbye!");
        System.out.println("=".repeat(60) + "\n");
    }

    public void stop() {
        this.running = false;
    }
}
