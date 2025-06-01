package com.syos.application.commands.inventory;

import com.syos.application.interfaces.Command;
import com.syos.application.services.InventoryService;
import com.syos.infrastructure.ui.presenters.InventoryPresenter;
import com.syos.infrastructure.ui.cli.InputReader;
import com.syos.domain.entities.Item;
import com.syos.domain.exceptions.*;
import java.util.List;

/**
 * Command to move items from store to shelf
 * Implements the Command pattern
 */
public class MoveToShelfCommand implements Command {
    private final InventoryService inventoryService;
    private final InventoryPresenter presenter;
    private final InputReader inputReader;

    public MoveToShelfCommand(InventoryService inventoryService,
                              InventoryPresenter presenter,
                              InputReader inputReader) {
        this.inventoryService = inventoryService;
        this.presenter = presenter;
        this.inputReader = inputReader;
    }

    @Override
    public void execute() {
        presenter.showHeader("Move Items to Shelf");

        try {
            // Show available items in store
            List<Item> storeItems = inventoryService.getItemsInStore();

            if (storeItems.isEmpty()) {
                presenter.showInfo("No items available in store to move to shelf.");
                return;
            }

            // Display items in store
            displayStoreItems(storeItems);

            // Get item code from user
            String itemCode = collectItemCode();

            // Find the item
            Item item = inventoryService.getItemByCode(itemCode);
            if (item == null) {
                presenter.showError("Item not found: " + itemCode);
                return;
            }

            // Check if item is in store
            if (!item.getState().getStateName().equals("IN_STORE")) {
                presenter.showError("Item is not in store. Current state: " + item.getState().getStateName());
                return;
            }

            // Show item details
            displayItemDetails(item);

            // Get quantity to move
            int quantityToMove = collectQuantity(item.getQuantity().getValue());

            // Confirm action
            if (confirmMove(item, quantityToMove)) {
                // Move items to shelf
                inventoryService.moveToShelf(itemCode, quantityToMove);

                presenter.showSuccess(String.format(
                        "Successfully moved %d units of %s to shelf",
                        quantityToMove, item.getName()
                ));

                // Show updated item status
                Item updatedItem = inventoryService.getItemByCode(itemCode);
                displayItemDetails(updatedItem);

            } else {
                presenter.showInfo("Move to shelf cancelled.");
            }

        } catch (InvalidStateTransitionException e) {
            presenter.showError("Cannot move items: " + e.getMessage());
        } catch (Exception e) {
            presenter.showError("Failed to move items to shelf: " + e.getMessage());
        }
    }

    private void displayStoreItems(List<Item> items) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Items Available in Store:");
        System.out.println("=".repeat(80));
        System.out.printf("%-15s %-30s %15s %15s%n",
                "Code", "Name", "Quantity", "Expiry Date");
        System.out.println("-".repeat(80));

        for (Item item : items) {
            System.out.printf("%-15s %-30s %15d %15s%n",
                    item.getCode().getValue(),
                    truncate(item.getName(), 30),
                    item.getQuantity().getValue(),
                    item.getExpiryDate() != null ? item.getExpiryDate() : "No expiry"
            );
        }
        System.out.println("-".repeat(80));
    }

    private void displayItemDetails(Item item) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Item Details:");
        System.out.println("=".repeat(50));
        System.out.println("Code: " + item.getCode().getValue());
        System.out.println("Name: " + item.getName());
        System.out.println("Current State: " + item.getState().getStateName());
        System.out.println("Quantity Available: " + item.getQuantity().getValue());
        System.out.println("Price: " + item.getPrice());
        if (item.getExpiryDate() != null) {
            System.out.println("Expiry Date: " + item.getExpiryDate());
            System.out.println("Days until expiry: " + item.daysUntilExpiry());
        }
        System.out.println("=".repeat(50));
    }

    private String collectItemCode() {
        return inputReader.readString("Enter item code to move: ").toUpperCase();
    }

    private int collectQuantity(int maxQuantity) {
        while (true) {
            System.out.println("Maximum available: " + maxQuantity);
            int quantity = inputReader.readInt("Enter quantity to move to shelf: ");

            if (quantity <= 0) {
                presenter.showError("Quantity must be greater than zero.");
            } else if (quantity > maxQuantity) {
                presenter.showError("Quantity exceeds available stock. Maximum: " + maxQuantity);
            } else {
                return quantity;
            }
        }
    }

    private boolean confirmMove(Item item, int quantity) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Confirm Move to Shelf:");
        System.out.println("=".repeat(50));
        System.out.println("Item: " + item.getName());
        System.out.println("Code: " + item.getCode().getValue());
        System.out.println("Quantity to move: " + quantity);
        System.out.println("Remaining in store: " + (item.getQuantity().getValue() - quantity));
        System.out.println("=".repeat(50));

        return inputReader.readBoolean("Confirm move?");
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    @Override
    public String getDescription() {
        return "Move Items to Shelf";
    }
}