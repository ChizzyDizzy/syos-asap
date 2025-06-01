package com.syos.infrastructure.ui.cli;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class InputReader {
    private static InputReader instance;
    private final Scanner scanner;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private InputReader() {
        this.scanner = new Scanner(System.in);
    }

    public static InputReader getInstance() {
        if (instance == null) {
            instance = new InputReader();
        }
        return instance;
    }

    public String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    public BigDecimal readBigDecimal(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return new BigDecimal(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please try again.");
            }
        }
    }

    public LocalDate readDate(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(input, DATE_FORMAT);
        } catch (Exception e) {
            System.out.println("Invalid date format. Using today's date.");
            return LocalDate.now();
        }
    }

    public boolean readBoolean(String prompt) {
        System.out.print(prompt + " (y/n): ");
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("y") || input.equals("yes");
    }
}