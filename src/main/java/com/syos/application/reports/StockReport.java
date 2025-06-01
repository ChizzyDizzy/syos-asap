package com.syos.application.reports;

import com.syos.domain.entities.Item;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StockReport extends AbstractReport {
    private final List<Item> items;

    public StockReport(List<Item> items) {
        this.items = items;
    }

    @Override
    protected String getReportTitle() {
        return "Current Stock Report";
    }

    @Override
    protected String getReportType() {
        return "STOCK_REPORT";
    }

    @Override
    protected String generateBody() {
        StringBuilder body = new StringBuilder();

        // Group items by state
        Map<String, List<Item>> itemsByState = items.stream()
                .collect(Collectors.groupingBy(item -> item.getState().getStateName()));

        for (Map.Entry<String, List<Item>> entry : itemsByState.entrySet()) {
            body.append("\n").append(entry.getKey()).append(" Items:\n");
            body.append("-".repeat(80)).append("\n");
            body.append(String.format("%-15s %-30s %10s %15s %15s%n",
                    "Code", "Name", "Quantity", "Purchase Date", "Expiry Date"));
            body.append("-".repeat(80)).append("\n");

            for (Item item : entry.getValue()) {
                body.append(String.format("%-15s %-30s %10d %15s %15s%n",
                        item.getCode().getValue(),
                        item.getName(),
                        item.getQuantity().getValue(),
                        item.getPurchaseDate(),
                        item.getExpiryDate() != null ? item.getExpiryDate() : "N/A"));
            }
        }

        return body.toString();
    }

    @Override
    protected String generateSummary() {
        int totalItems = items.size();
        int totalQuantity = items.stream()
                .mapToInt(item -> item.getQuantity().getValue())
                .sum();

        Map<String, Long> countByState = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getState().getStateName(),
                        Collectors.counting()
                ));

        StringBuilder summary = new StringBuilder("\nStock Summary:\n");
        summary.append("Total Item Types: ").append(totalItems).append("\n");
        summary.append("Total Quantity: ").append(totalQuantity).append("\n");
        summary.append("\nItems by State:\n");

        for (Map.Entry<String, Long> entry : countByState.entrySet()) {
            summary.append("  ").append(entry.getKey()).append(": ")
                    .append(entry.getValue()).append("\n");
        }

        return summary.toString();
    }
}
