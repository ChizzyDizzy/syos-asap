package com.syos.infrastructure.ui.presenters;

import com.syos.domain.entities.Item;
import java.util.List;

public class InventoryPresenter {

    public void showItems(List<Item> items) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Inventory List");
        System.out.println("=".repeat(80));
        System.out.printf("%-15s %-30s %10s %10s %12s %15s%n",
                "Code", "Name", "Price", "Quantity", "State", "Expiry Date");
        System.out.println("-".repeat(80));

        for (Item item : items) {
            System.out.printf("%-15s %-30s %10s %10d %12s %15s%n",
                    item.getCode().getValue(),
                    truncate(item.getName(), 30),
                    item.getPrice(),
                    item.getQuantity().getValue(),
                    item.getState().getStateName(),
                    item.getExpiryDate() != null ? item.getExpiryDate() : "N/A");
        }

        System.out.println("-".repeat(80));
        System.out.println("Total items: " + items.size());
    }

    public void showSuccess(String message) {
        System.out.println("✓ " + message);
    }

    public void showError(String message) {
        System.err.println("❌ Error: " + message);
    }

    public void showHeader(String title) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(title);
        System.out.println("=".repeat(50));
    }

    public void showInfo(String message) {
        System.out.println("ℹ️  " + message);
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}