package com.syos.application.commands.sales;

import com.syos.application.interfaces.Command;
import com.syos.application.services.SalesService;
import com.syos.infrastructure.ui.presenters.SalesPresenter;
import com.syos.infrastructure.ui.cli.InputReader;
import com.syos.domain.exceptions.*;
import java.math.BigDecimal;

public class CreateSaleCommand implements Command {
    private final SalesService salesService;
    private final SalesPresenter presenter;
    private final InputReader inputReader;

    public CreateSaleCommand(SalesService salesService,
                             SalesPresenter presenter,
                             InputReader inputReader) {
        this.salesService = salesService;
        this.presenter = presenter;
        this.inputReader = inputReader;
    }

    @Override
    public void execute() {
        presenter.showSaleHeader();

        // Show available items first (helpful for users)
        showAvailableItems();

        // Build the sale
        SalesService.SaleBuilder saleBuilder = salesService.startNewSale();

        int itemCount = 0;
        boolean addingItems = true;

        while (addingItems) {
            System.out.println(); // Add spacing
            String itemCode = inputReader.readString("Enter item code (or 'DONE' to finish): ").trim();

            if ("DONE".equalsIgnoreCase(itemCode)) {
                if (itemCount == 0) {
                    presenter.showError("No items added to sale. Sale cancelled.");
                    return;
                }
                addingItems = false;
                continue;
            }

            try {
                // Make sure code is uppercase
                itemCode = itemCode.toUpperCase();

                // Show item details before adding
                if (!salesService.isItemAvailable(itemCode)) {
                    presenter.showError("Item " + itemCode + " not found or not available for sale");
                    continue;
                }

                int quantity = inputReader.readInt("Enter quantity: ");

                if (quantity <= 0) {
                    presenter.showError("Quantity must be greater than zero");
                    continue;
                }

                // Add item to sale
                saleBuilder.addItem(itemCode, quantity);
                presenter.showItemAdded(itemCode, quantity);
                itemCount++;

                // Show running total
                presenter.showSubtotal(saleBuilder.getSubtotal());

            } catch (ItemNotFoundException e) {
                presenter.showError("Item not found: " + e.getMessage());
            } catch (InsufficientStockException e) {
                presenter.showError("Insufficient stock: " + e.getMessage());
            } catch (Exception e) {
                presenter.showError("Error adding item: " + e.getMessage());
            }
        }

        // Process payment
        try {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("PAYMENT");
            System.out.println("=".repeat(50));

            BigDecimal subtotal = saleBuilder.getSubtotal().getValue();
            presenter.showSubtotal(saleBuilder.getSubtotal());

            BigDecimal cashTendered = inputReader.readBigDecimal("Enter cash amount: $");

            // Validate cash amount
            if (cashTendered.compareTo(subtotal) < 0) {
                presenter.showError("Insufficient cash. Required: $" + subtotal);
                return;
            }

            // Complete the sale
            var bill = saleBuilder.completeSale(cashTendered);

            // Save and display bill
            salesService.saveBill(bill);
            presenter.showBill(bill);

            presenter.showSuccess("Sale completed successfully!");

        } catch (EmptySaleException e) {
            presenter.showError("Cannot complete sale: " + e.getMessage());
        } catch (Exception e) {
            presenter.showError("Sale failed: " + e.getMessage());
            e.printStackTrace(); // For debugging
        }
    }

    private void showAvailableItems() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("AVAILABLE ITEMS");
        System.out.println("=".repeat(60));

        try {
            var availableItems = salesService.getAvailableItems();

            if (availableItems.isEmpty()) {
                presenter.showWarning("No items available for sale!");
                presenter.showInfo("Please move items to shelf first (Inventory → Move to Shelf)");
                return;
            }

            System.out.printf("%-15s %-30s %10s %10s%n", "Code", "Name", "Price", "Stock");
            System.out.println("-".repeat(60));

            for (var item : availableItems) {
                System.out.printf("%-15s %-30s $%9.2f %10d%n",
                        item.getCode().getValue(),
                        truncate(item.getName(), 30),
                        item.getPrice().getValue(),
                        item.getQuantity().getValue()
                );
            }
            System.out.println("-".repeat(60));

        } catch (Exception e) {
            presenter.showError("Could not load available items");
        }
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    @Override
    public String getDescription() {
        return "Create New Sale";
    }
}