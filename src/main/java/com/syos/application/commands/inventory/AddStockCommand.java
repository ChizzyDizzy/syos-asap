package com.syos.application.commands.inventory;

import com.syos.application.interfaces.Command;
import com.syos.application.services.InventoryService;
import com.syos.infrastructure.ui.presenters.InventoryPresenter;
import com.syos.infrastructure.ui.cli.InputReader;
import com.syos.domain.exceptions.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Command to add new stock to inventory
 * Implements the Command pattern
 */
public class AddStockCommand implements Command {
    private final InventoryService inventoryService;
    private final InventoryPresenter presenter;
    private final InputReader inputReader;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public AddStockCommand(InventoryService inventoryService,
                           InventoryPresenter presenter,
                           InputReader inputReader) {
        this.inventoryService = inventoryService;
        this.presenter = presenter;
        this.inputReader = inputReader;
    }

    @Override
    public void execute() {
        presenter.showHeader("Add New Stock");

        try {
            // Collect stock information
            String code = collectItemCode();
            String name = collectItemName();
            BigDecimal price = collectPrice();
            int quantity = collectQuantity();
            LocalDate expiryDate = collectExpiryDate();

            // Confirm before adding
            if (confirmAddStock(code, name, price, quantity, expiryDate)) {
                // Add stock to inventory
                inventoryService.addStock(code, name, price, quantity, expiryDate);

                presenter.showSuccess(String.format(
                        "Stock added successfully: %s - %s (Qty: %d)",
                        code, name, quantity
                ));
            } else {
                presenter.showError("Stock addition cancelled.");
            }

        } catch (Exception e) {
            presenter.showError("Failed to add stock: " + e.getMessage());
        }
    }

    private String collectItemCode() {
        while (true) {
            String code = inputReader.readString("Enter item code (e.g., ITEM001): ").toUpperCase();

            if (code.matches("^[A-Z0-9]{4,10}$")) {
                return code;
            } else {
                presenter.showError("Invalid code format. Use 4-10 alphanumeric characters.");
            }
        }
    }

    private String collectItemName() {
        while (true) {
            String name = inputReader.readString("Enter item name: ").trim();

            if (!name.isEmpty() && name.length() <= 100) {
                return name;
            } else {
                presenter.showError("Item name must be between 1 and 100 characters.");
            }
        }
    }

    private BigDecimal collectPrice() {
        while (true) {
            try {
                BigDecimal price = inputReader.readBigDecimal("Enter price per unit: $");

                if (price.compareTo(BigDecimal.ZERO) > 0) {
                    return price;
                } else {
                    presenter.showError("Price must be greater than zero.");
                }
            } catch (NumberFormatException e) {
                presenter.showError("Invalid price format. Please enter a valid number.");
            }
        }
    }

    private int collectQuantity() {
        while (true) {
            try {
                int quantity = inputReader.readInt("Enter quantity: ");

                if (quantity > 0) {
                    return quantity;
                } else {
                    presenter.showError("Quantity must be greater than zero.");
                }
            } catch (NumberFormatException e) {
                presenter.showError("Invalid quantity. Please enter a whole number.");
            }
        }
    }

    private LocalDate collectExpiryDate() {
        System.out.println("Enter expiry date (YYYY-MM-DD) or press Enter for no expiry: ");
        String dateStr = inputReader.readString("").trim();

        if (dateStr.isEmpty()) {
            return null; // No expiry date
        }

        while (true) {
            try {
                LocalDate expiryDate = LocalDate.parse(dateStr, DATE_FORMAT);

                if (expiryDate.isAfter(LocalDate.now())) {
                    return expiryDate;
                } else {
                    presenter.showError("Expiry date must be in the future.");
                    dateStr = inputReader.readString("Enter expiry date (YYYY-MM-DD): ").trim();
                }
            } catch (DateTimeParseException e) {
                presenter.showError("Invalid date format. Use YYYY-MM-DD.");
                dateStr = inputReader.readString("Enter expiry date (YYYY-MM-DD): ").trim();
            }
        }
    }

    private boolean confirmAddStock(String code, String name, BigDecimal price,
                                    int quantity, LocalDate expiryDate) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Confirm Stock Addition:");
        System.out.println("=".repeat(50));
        System.out.println("Code: " + code);
        System.out.println("Name: " + name);
        System.out.println("Price: $" + price);
        System.out.println("Quantity: " + quantity);
        System.out.println("Expiry Date: " + (expiryDate != null ? expiryDate : "No expiry"));
        System.out.println("=".repeat(50));

        return inputReader.readBoolean("Confirm addition?");
    }

    @Override
    public String getDescription() {
        return "Add New Stock";
    }
}