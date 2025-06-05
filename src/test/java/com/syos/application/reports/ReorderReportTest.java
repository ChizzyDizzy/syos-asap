package com.syos.application.reports;

import com.syos.domain.entities.Item;
import com.syos.domain.valueobjects.ItemCode;
import com.syos.domain.valueobjects.Quantity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Essential Clean Unit Tests for ReorderReport
 * 3 focused tests covering core functionality, edge cases, and design patterns
 */
@DisplayName("SYOS ReorderReport - Essential Tests")
class ReorderReportTest {

    private Item createLowStockItem(String code, String name, int currentStock) {
        Item item = mock(Item.class);
        ItemCode itemCode = mock(ItemCode.class);
        Quantity quantity = mock(Quantity.class);

        when(itemCode.getValue()).thenReturn(code);
        when(quantity.getValue()).thenReturn(currentStock);
        when(item.getCode()).thenReturn(itemCode);
        when(item.getName()).thenReturn(name);
        when(item.getQuantity()).thenReturn(quantity);

        return item;
    }

    @Test
    @DisplayName("Should generate complete reorder report with low stock items and calculations")
    void should_generate_reorder_report_with_low_stock_items() {
        // Arrange - SYOS items below reorder threshold
        List<Item> lowStockItems = Arrays.asList(
                createLowStockItem("MILK001", "Highland Fresh Milk 1L", 15),
                createLowStockItem("BREAD002", "Prima Bread Loaf", 8)
        );
        int reorderThreshold = 50;
        ReorderReport reorderReport = new ReorderReport(lowStockItems, reorderThreshold);

        // Act
        String reportBody = reorderReport.generateBody();
        String reportSummary = reorderReport.generateSummary();

        // Assert - Verify complete report structure and calculations
        assertTrue(reportBody.contains("Items Below Reorder Level (50):"));
        assertTrue(reportBody.contains("MILK001"));
        assertTrue(reportBody.contains("Highland Fresh Milk 1L"));
        assertTrue(reportBody.contains("15")); // Current stock
        assertTrue(reportBody.contains("85")); // Suggested order (50*2-15)
        assertTrue(reportBody.contains("BREAD002"));
        assertTrue(reportBody.contains("92")); // Suggested order (50*2-8)

        assertTrue(reportSummary.contains("Items requiring reorder: 2"));
        assertTrue(reportSummary.contains("Reorder threshold: 50 units"));
    }

    @Test
    @DisplayName("Should handle empty stock list gracefully with appropriate messaging")
    void should_handle_empty_stock_list() {
        // Arrange - No items need reordering
        List<Item> emptyStockList = new ArrayList<>();
        int reorderThreshold = 50;
        ReorderReport reorderReport = new ReorderReport(emptyStockList, reorderThreshold);

        // Act
        String reportBody = reorderReport.generateBody();
        String reportSummary = reorderReport.generateSummary();

        // Assert - Verify empty state handling
        assertTrue(reportBody.contains("No items require reordering at this time."));
        assertFalse(reportBody.contains("Code")); // No table headers

        assertTrue(reportSummary.contains("Items requiring reorder: 0"));
        assertTrue(reportSummary.contains("Reorder threshold: 50 units"));
    }

    @Test
    @DisplayName("Should properly implement Template Method pattern with correct report metadata")
    void should_implement_template_method_pattern_correctly() {
        // Arrange
        List<Item> lowStockItems = Arrays.asList(createLowStockItem("TEST001", "Test Item", 10));
        ReorderReport reorderReport = new ReorderReport(lowStockItems, 25);

        // Act & Assert - Verify Template Method pattern implementation
        assertEquals("Reorder Level Report", reorderReport.getReportTitle());
        assertEquals("REORDER_REPORT", reorderReport.getReportType());

        // Verify it extends AbstractReport
        assertTrue(reorderReport instanceof AbstractReport);

        // Verify all template methods are implemented
        assertDoesNotThrow(() -> {
            reorderReport.getReportTitle();
            reorderReport.getReportType();
            reorderReport.generateBody();
            reorderReport.generateSummary();
        });
    }
}