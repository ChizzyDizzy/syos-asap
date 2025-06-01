package com.syos.application.reports;

import com.syos.domain.entities.Item;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Report for items that need to be reshelved at end of day
 * This includes items expiring soon that should be moved to front
 * or items that need to be removed from shelves
 */
public class ReshelveReport extends AbstractReport {
    private final List<Item> expiringItems;
    private final LocalDate currentDate;
    private final int daysThreshold;

    public ReshelveReport(List<Item> expiringItems) {
        this(expiringItems, 7); // Default to 7 days threshold
    }

    public ReshelveReport(List<Item> expiringItems, int daysThreshold) {
        this.expiringItems = expiringItems;
        this.currentDate = LocalDate.now();
        this.daysThreshold = daysThreshold;
    }

    @Override
    protected String getReportTitle() {
        return "Items to Reshelve Report";
    }

    @Override
    protected String getReportType() {
        return "RESHELVE_REPORT";
    }

    @Override
    protected String generateBody() {
        StringBuilder body = new StringBuilder();

        // Group items by urgency
        Map<ReshelveUrgency, List<Item>> itemsByUrgency = groupItemsByUrgency();

        // Critical items (expiring today or tomorrow)
        if (itemsByUrgency.containsKey(ReshelveUrgency.CRITICAL)) {
            body.append("\n🚨 CRITICAL - Remove from shelf immediately:\n");
            body.append("-".repeat(80)).append("\n");
            appendItemsTable(body, itemsByUrgency.get(ReshelveUrgency.CRITICAL));
        }

        // High priority items (expiring in 2-3 days)
        if (itemsByUrgency.containsKey(ReshelveUrgency.HIGH)) {
            body.append("\n⚠️  HIGH PRIORITY - Move to front of shelf:\n");
            body.append("-".repeat(80)).append("\n");
            appendItemsTable(body, itemsByUrgency.get(ReshelveUrgency.HIGH));
        }

        // Medium priority items (expiring in 4-7 days)
        if (itemsByUrgency.containsKey(ReshelveUrgency.MEDIUM)) {
            body.append("\n📋 MEDIUM PRIORITY - Rotate stock:\n");
            body.append("-".repeat(80)).append("\n");
            appendItemsTable(body, itemsByUrgency.get(ReshelveUrgency.MEDIUM));
        }

        if (expiringItems.isEmpty()) {
            body.append("\n✓ No items require reshelving at this time.\n");
        }

        return body.toString();
    }

    @Override
    protected String generateSummary() {
        Map<ReshelveUrgency, List<Item>> itemsByUrgency = groupItemsByUrgency();

        int criticalCount = itemsByUrgency.getOrDefault(ReshelveUrgency.CRITICAL, List.of()).size();
        int highCount = itemsByUrgency.getOrDefault(ReshelveUrgency.HIGH, List.of()).size();
        int mediumCount = itemsByUrgency.getOrDefault(ReshelveUrgency.MEDIUM, List.of()).size();
        int totalCount = expiringItems.size();

        StringBuilder summary = new StringBuilder("\nReshelve Summary:\n");
        summary.append("Total items requiring attention: ").append(totalCount).append("\n");

        if (totalCount > 0) {
            summary.append("  - Critical (Remove): ").append(criticalCount).append("\n");
            summary.append("  - High Priority (Front): ").append(highCount).append("\n");
            summary.append("  - Medium Priority (Rotate): ").append(mediumCount).append("\n");
        }

        summary.append("\nRecommendations:\n");
        if (criticalCount > 0) {
            summary.append("• URGENT: Remove ").append(criticalCount)
                    .append(" critical items from shelves immediately\n");
        }
        if (highCount > 0) {
            summary.append("• Move ").append(highCount)
                    .append(" high-priority items to front of shelf for quick sale\n");
        }
        if (mediumCount > 0) {
            summary.append("• Rotate ").append(mediumCount)
                    .append(" medium-priority items during regular restocking\n");
        }

        return summary.toString();
    }

    private void appendItemsTable(StringBuilder body, List<Item> items) {
        body.append(String.format("%-15s %-30s %10s %15s %10s %12s%n",
                "Code", "Name", "Quantity", "Expiry Date", "Days Left", "Action"));
        body.append("-".repeat(80)).append("\n");

        for (Item item : items) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(currentDate, item.getExpiryDate());
            String action = determineAction(daysUntilExpiry);

            body.append(String.format("%-15s %-30s %10d %15s %10d %12s%n",
                    item.getCode().getValue(),
                    truncate(item.getName(), 30),
                    item.getQuantity().getValue(),
                    item.getExpiryDate(),
                    daysUntilExpiry,
                    action));
        }
        body.append("\n");
    }

    private Map<ReshelveUrgency, List<Item>> groupItemsByUrgency() {
        return expiringItems.stream()
                .collect(Collectors.groupingBy(this::categorizeUrgency));
    }

    private ReshelveUrgency categorizeUrgency(Item item) {
        long daysUntilExpiry = ChronoUnit.DAYS.between(currentDate, item.getExpiryDate());

        if (daysUntilExpiry <= 1) {
            return ReshelveUrgency.CRITICAL;
        } else if (daysUntilExpiry <= 3) {
            return ReshelveUrgency.HIGH;
        } else {
            return ReshelveUrgency.MEDIUM;
        }
    }

    private String determineAction(long daysUntilExpiry) {
        if (daysUntilExpiry <= 1) {
            return "REMOVE";
        } else if (daysUntilExpiry <= 3) {
            return "MOVE FRONT";
        } else {
            return "ROTATE";
        }
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    private enum ReshelveUrgency {
        CRITICAL,  // Expiring today or tomorrow
        HIGH,      // Expiring in 2-3 days
        MEDIUM     // Expiring in 4-7 days
    }
}