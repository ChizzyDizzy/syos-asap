package com.syos.application.reports;

import com.syos.domain.entities.Item;
import java.util.List;

public class ReorderReport extends AbstractReport {
    private final List<Item> lowStockItems;
    private final int reorderThreshold;

    public ReorderReport(List<Item> lowStockItems, int reorderThreshold) {
        this.lowStockItems = lowStockItems;
        this.reorderThreshold = reorderThreshold;
    }

    @Override
    protected String getReportTitle() {
        return "Reorder Level Report";
    }

    @Override
    protected String getReportType() {
        return "REORDER_REPORT";
    }

    @Override
    protected String generateBody() {
        StringBuilder body = new StringBuilder();
        body.append("\nItems Below Reorder Level (").append(reorderThreshold).append("):\n");
        body.append("-".repeat(80)).append("\n");

        if (lowStockItems.isEmpty()) {
            body.append("No items require reordering at this time.\n");
        } else {
            body.append(String.format("%-15s %-30s %15s %15s%n",
                    "Code", "Name", "Current Stock", "Suggested Order"));
            body.append("-".repeat(80)).append("\n");

            for (Item item : lowStockItems) {
                int currentStock = item.getQuantity().getValue();
                int suggestedOrder = reorderThreshold * 2 - currentStock;

                body.append(String.format("%-15s %-30s %15d %15d%n",
                        item.getCode().getValue(),
                        item.getName(),
                        currentStock,
                        suggestedOrder));
            }
        }

        return body.toString();
    }

    @Override
    protected String generateSummary() {
        return "\nSummary:\n" +
                "Items requiring reorder: " + lowStockItems.size() + "\n" +
                "Reorder threshold: " + reorderThreshold + " units\n";
    }
}