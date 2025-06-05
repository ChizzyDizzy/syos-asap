package com.syos.application.reports;

import com.syos.domain.entities.Bill;
import com.syos.domain.entities.Item;
import com.syos.domain.valueobjects.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DailySalesReportTest {

    @Test
    void should_aggregate_multiple_bills_with_same_items_correctly() {
        // Arrange
        LocalDate reportDate = LocalDate.of(2024, 6, 5);

        // Create test items
        Item rice = createTestItem("RICE001", "Basmati Rice", new BigDecimal("150.00"));
        Item oil = createTestItem("OIL001", "Cooking Oil", new BigDecimal("300.00"));

        // Create bills with overlapping items
        Bill bill1 = new Bill.Builder()
                .withBillNumber(1)
                .withDate(LocalDateTime.of(2024, 6, 5, 10, 0))
                .addItem(rice, 2)  // 2 * 150 = 300
                .addItem(oil, 1)   // 1 * 300 = 300
                .withCashTendered(new BigDecimal("600.00"))
                .build();

        Bill bill2 = new Bill.Builder()
                .withBillNumber(2)
                .withDate(LocalDateTime.of(2024, 6, 5, 14, 0))
                .addItem(rice, 3)  // 3 * 150 = 450
                .addItem(oil, 2)   // 2 * 300 = 600
                .withCashTendered(new BigDecimal("1050.00"))
                .build();

        List<Bill> bills = Arrays.asList(bill1, bill2);

        // Act
        DailySalesReport report = new DailySalesReport(reportDate, bills);

        // Assert - Test aggregation behavior
        assertNotNull(report);
        assertEquals("Daily Sales Report - 2024-06-05", report.getReportTitle());
        assertEquals("DAILY_SALES", report.getReportType());

        // Test report content generation
        String reportBody = report.generateBody();
        assertTrue(reportBody.contains("RICE001"));
        assertTrue(reportBody.contains("Basmati Rice"));
        assertTrue(reportBody.contains("OIL001"));
        assertTrue(reportBody.contains("Cooking Oil"));

        String summary = report.generateSummary();
        assertTrue(summary.contains("Total Transactions: 2"));
        assertTrue(summary.contains("Total Items Sold: 8")); // 2+1+3+2 = 8
        assertTrue(summary.contains("Total Revenue: $1650.00")); // 300+300+450+600 = 1650
    }

    @Test
    void should_handle_empty_bills_list_gracefully() {
        // Arrange
        LocalDate reportDate = LocalDate.of(2024, 6, 5);
        List<Bill> emptyBills = new ArrayList<>();

        // Act
        DailySalesReport report = new DailySalesReport(reportDate, emptyBills);

        // Assert
        assertEquals("Daily Sales Report - 2024-06-05", report.getReportTitle());
        assertEquals("DAILY_SALES", report.getReportType());

        String reportBody = report.generateBody();
        assertTrue(reportBody.contains("Sales Summary by Item:"));
        assertTrue(reportBody.contains("Item Code"));
        assertTrue(reportBody.contains("Item Name"));
        assertTrue(reportBody.contains("Quantity"));
        assertTrue(reportBody.contains("Total Revenue"));

        String summary = report.generateSummary();
        assertTrue(summary.contains("Total Transactions: 0"));
        assertTrue(summary.contains("Total Items Sold: 0"));
        assertTrue(summary.contains("Total Revenue: $0.00"));
    }

    @Test
    void should_format_report_body_with_correct_structure_and_data() {
        // Arrange
        LocalDate reportDate = LocalDate.of(2024, 6, 5);
        Item testItem = createTestItem("TEST001", "Test Product", new BigDecimal("99.99"));

        Bill testBill = new Bill.Builder()
                .withBillNumber(100)
                .withDate(LocalDateTime.of(2024, 6, 5, 12, 0))
                .addItem(testItem, 5) // 5 * 99.99 = 499.95
                .withCashTendered(new BigDecimal("500.00"))
                .build();

        List<Bill> bills = Arrays.asList(testBill);

        // Act
        DailySalesReport report = new DailySalesReport(reportDate, bills);
        String reportBody = report.generateBody();
        String summary = report.generateSummary();

        // Assert - Verify report structure and formatting
        assertTrue(reportBody.contains("Sales Summary by Item:"));
        assertTrue(reportBody.contains("--------")); // Dash lines for formatting
        assertTrue(reportBody.contains("TEST001")); // Item code
        assertTrue(reportBody.contains("Test Product")); // Item name
        assertTrue(reportBody.contains("5")); // Quantity
        assertTrue(reportBody.contains("$499.95")); // Total revenue

        // Verify summary calculations
        assertTrue(summary.contains("Total Transactions: 1"));
        assertTrue(summary.contains("Total Items Sold: 5"));
        assertTrue(summary.contains("Total Revenue: $499.95"));

        // Check that the report has proper table structure
        String[] lines = reportBody.split("\n");
        boolean hasHeaderLine = false;
        for (String line : lines) {
            if (line.contains("Item Code") && line.contains("Item Name") &&
                    line.contains("Quantity") && line.contains("Total Revenue")) {
                hasHeaderLine = true;
                break;
            }
        }
        assertTrue(hasHeaderLine, "Report should contain proper table headers");
    }

    // Helper method to create test items using Builder pattern
    private Item createTestItem(String code, String name, BigDecimal price) {
        // Use Item Builder pattern (similar to Bill.Builder)
        return new Item.Builder()
                .withCode(code)
                .withName(name)
                .withPrice(price)
                .build();
    }
}