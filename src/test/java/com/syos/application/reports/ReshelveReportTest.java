package com.syos.application.reports;

import com.syos.domain.entities.Item;
import com.syos.domain.valueobjects.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReshelveReportTest {

    @Test
    void should_categorize_items_by_urgency_based_on_expiry_dates() {
        // Arrange
        LocalDate today = LocalDate.now();

        // Create items with different expiry dates for different urgency levels
        Item criticalItem = createTestItem("CRIT001", "Critical Item", 10, today.plusDays(1)); // Critical - expires tomorrow
        Item highItem = createTestItem("HIGH001", "High Priority Item", 15, today.plusDays(3)); // High - expires in 3 days
        Item mediumItem = createTestItem("MED001", "Medium Priority Item", 20, today.plusDays(6)); // Medium - expires in 6 days

        List<Item> expiringItems = Arrays.asList(criticalItem, highItem, mediumItem);
        ReshelveReport report = new ReshelveReport(expiringItems);

        // Act
        String reportBody = report.generateBody();
        String summary = report.generateSummary();

        // Assert - Verify categorization and content
        assertTrue(reportBody.contains("🚨 CRITICAL - Remove from shelf immediately:"));
        assertTrue(reportBody.contains("CRIT001"));
        assertTrue(reportBody.contains("REMOVE"));

        assertTrue(reportBody.contains("⚠️  HIGH PRIORITY - Move to front of shelf:"));
        assertTrue(reportBody.contains("HIGH001"));
        assertTrue(reportBody.contains("MOVE FRONT"));

        assertTrue(reportBody.contains("📋 MEDIUM PRIORITY - Rotate stock:"));
        assertTrue(reportBody.contains("MED001"));
        assertTrue(reportBody.contains("ROTATE"));

        // Verify summary counts
        assertTrue(summary.contains("Total items requiring attention: 3"));
        assertTrue(summary.contains("Critical (Remove): 1"));
        assertTrue(summary.contains("High Priority (Front): 1"));
        assertTrue(summary.contains("Medium Priority (Rotate): 1"));
    }

    @Test
    void should_handle_empty_items_list_gracefully() {
        // Arrange
        List<Item> emptyItems = new ArrayList<>();
        ReshelveReport report = new ReshelveReport(emptyItems);

        // Act
        String reportBody = report.generateBody();
        String summary = report.generateSummary();

        // Assert
        assertEquals("Items to Reshelve Report", report.getReportTitle());
        assertEquals("RESHELVE_REPORT", report.getReportType());

        // Verify empty state handling
        assertTrue(reportBody.contains("✓ No items require reshelving at this time."));
        assertFalse(reportBody.contains("🚨 CRITICAL"));
        assertFalse(reportBody.contains("⚠️  HIGH PRIORITY"));
        assertFalse(reportBody.contains("📋 MEDIUM PRIORITY"));

        // Verify summary for empty list
        assertTrue(summary.contains("Total items requiring attention: 0"));
        assertFalse(summary.contains("URGENT: Remove"));
    }

    @Test
    void should_format_report_correctly_with_proper_table_structure_and_recommendations() {
        // Arrange
        LocalDate today = LocalDate.now();

        // Create multiple critical items to test formatting and recommendations
        Item expiredItem = createTestItem("EXP001", "Already Expired Item", 5, today.minusDays(1)); // Critical
        Item expiringToday = createTestItem("TODAY001", "Expiring Today Item", 8, today); // Critical
        Item expiringIn2Days = createTestItem("SOON001", "Expiring Soon Item", 12, today.plusDays(2)); // High

        List<Item> items = Arrays.asList(expiredItem, expiringToday, expiringIn2Days);
        ReshelveReport report = new ReshelveReport(items, 7); // 7 days threshold

        // Act
        String reportBody = report.generateBody();
        String summary = report.generateSummary();

        // Assert - Verify table structure and formatting
        assertTrue(reportBody.contains("Code"));
        assertTrue(reportBody.contains("Name"));
        assertTrue(reportBody.contains("Quantity"));
        assertTrue(reportBody.contains("Expiry Date"));
        assertTrue(reportBody.contains("Days Left"));
        assertTrue(reportBody.contains("Action"));

        // Verify data formatting
        assertTrue(reportBody.contains("EXP001"));
        assertTrue(reportBody.contains("Already Expired Item"));
        assertTrue(reportBody.contains("5")); // quantity
        assertTrue(reportBody.contains("REMOVE")); // action for critical

        assertTrue(reportBody.contains("TODAY001"));
        assertTrue(reportBody.contains("Expiring Today Item"));
        assertTrue(reportBody.contains("8")); // quantity

        assertTrue(reportBody.contains("SOON001"));
        assertTrue(reportBody.contains("Expiring Soon Item"));
        assertTrue(reportBody.contains("MOVE FRONT")); // action for high priority

        // Verify recommendations in summary
        assertTrue(summary.contains("URGENT: Remove 2 critical items from shelves immediately"));
        assertTrue(summary.contains("Move 1 high-priority items to front of shelf"));

        // Verify dash lines for formatting
        long dashLineCount = reportBody.lines()
                .filter(line -> line.contains("--------"))
                .count();
        assertTrue(dashLineCount >= 2, "Report should have proper table formatting with dash lines");

        // Verify total count
        assertTrue(summary.contains("Total items requiring attention: 3"));
    }

    // Helper method to create test items
    // Adjust this method based on your actual Item constructor/builder
    private Item createTestItem(String code, String name, int quantity, LocalDate expiryDate) {
        // Option 1: If Item uses Builder pattern (adjust method names as needed)
        try {
            return new Item.Builder()
                    .withCode(code)
                    .withName(name)
                    .withQuantity(quantity)
                    .withExpiryDate(expiryDate)
                    .withPrice(new BigDecimal("100.00")) // Default price
                    .build();
        } catch (Exception e) {
            // Option 2: If different constructor exists
            // Adjust based on your actual Item implementation
            ItemCode itemCode = new ItemCode(code);
            Quantity qty = new Quantity(quantity);
            Money price = new Money(new BigDecimal("100.00"));

            // This is a placeholder - replace with your actual Item constructor
            throw new UnsupportedOperationException(
                    "Please adjust createTestItem method to match your Item class constructor. " +
                            "Item needs: code=" + code + ", name=" + name + ", quantity=" + quantity +
                            ", expiryDate=" + expiryDate);
        }
    }
}