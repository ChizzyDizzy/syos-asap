package com.syos.application.reports;

import com.syos.domain.entities.Item;
import com.syos.domain.valueobjects.ItemCode;
import com.syos.domain.valueobjects.Quantity;
import com.syos.domain.interfaces.ItemState;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Essential Clean Unit Tests for StockReport
 * 3 focused tests covering core functionality, edge cases, and design patterns
 */
@DisplayName("SYOS StockReport - Essential Tests")
class StockReportTest {

    private Item createStockItem(String code, String name, int quantity, String stateName, LocalDate purchaseDate, LocalDate expiryDate) {
        Item item = mock(Item.class);
        ItemCode itemCode = mock(ItemCode.class);
        Quantity itemQuantity = mock(Quantity.class);
        ItemState itemState = mock(ItemState.class);

        when(itemCode.getValue()).thenReturn(code);
        when(itemQuantity.getValue()).thenReturn(quantity);
        when(itemState.getStateName()).thenReturn(stateName);
        when(item.getCode()).thenReturn(itemCode);
        when(item.getName()).thenReturn(name);
        when(item.getQuantity()).thenReturn(itemQuantity);
        when(item.getState()).thenReturn(itemState);
        when(item.getPurchaseDate()).thenReturn(purchaseDate);
        when(item.getExpiryDate()).thenReturn(expiryDate);

        return item;
    }

    @Test
    @DisplayName("Should generate complete stock report with items grouped by state and accurate summary")
    void should_generate_stock_report_with_state_grouping_and_summary() {
        // Arrange - SYOS inventory with mixed states
        List<Item> stockItems = Arrays.asList(
                createStockItem("MILK001", "Highland Fresh Milk", 45, "IN_STORE", LocalDate.now().minusDays(1), LocalDate.now().plusDays(5)),
                createStockItem("BREAD002", "Prima Bread", 30, "ON_SHELF", LocalDate.now().minusDays(2), LocalDate.now().plusDays(2)),
                createStockItem("RICE003", "Basmati Rice", 80, "IN_STORE", LocalDate.now().minusDays(3), null),
                createStockItem("TEA004", "Ceylon Tea", 0, "SOLD", LocalDate.now().minusDays(5), null)
        );
        StockReport stockReport = new StockReport(stockItems);

        // Act
        String reportBody = stockReport.generateBody();
        String reportSummary = stockReport.generateSummary();

        // Assert - Verify grouping by state and calculations
        assertTrue(reportBody.contains("IN_STORE Items:"));
        assertTrue(reportBody.contains("ON_SHELF Items:"));
        assertTrue(reportBody.contains("SOLD Items:"));
        assertTrue(reportBody.contains("MILK001"));
        assertTrue(reportBody.contains("Highland Fresh Milk"));
        assertTrue(reportBody.contains("BREAD002"));
        assertTrue(reportBody.contains("N/A")); // For items without expiry

        assertTrue(reportSummary.contains("Total Item Types: 4"));
        assertTrue(reportSummary.contains("Total Quantity: 155")); // 45+30+80+0
        assertTrue(reportSummary.contains("IN_STORE: 2"));
        assertTrue(reportSummary.contains("ON_SHELF: 1"));
        assertTrue(reportSummary.contains("SOLD: 1"));
    }

    @Test
    @DisplayName("Should handle empty stock list gracefully")
    void should_handle_empty_stock_list() {
        // Arrange - No stock items
        List<Item> emptyStockList = new ArrayList<>();
        StockReport stockReport = new StockReport(emptyStockList);

        // Act
        String reportBody = stockReport.generateBody();
        String reportSummary = stockReport.generateSummary();

        // Assert - Verify empty state handling
        assertNotNull(reportBody);
        assertTrue(reportSummary.contains("Total Item Types: 0"));
        assertTrue(reportSummary.contains("Total Quantity: 0"));
        assertTrue(reportSummary.contains("Items by State:"));
    }

    @Test
    @DisplayName("Should properly implement Template Method pattern with correct report metadata")
    void should_implement_template_method_pattern_correctly() {
        // Arrange
        List<Item> stockItems = Arrays.asList(
                createStockItem("TEST001", "Test Item", 10, "IN_STORE", LocalDate.now(), null)
        );
        StockReport stockReport = new StockReport(stockItems);

        // Act & Assert - Verify Template Method pattern implementation
        assertEquals("Current Stock Report", stockReport.getReportTitle());
        assertEquals("STOCK_REPORT", stockReport.getReportType());

        // Verify it extends AbstractReport
        assertTrue(stockReport instanceof AbstractReport);

        // Verify all template methods are implemented
        assertDoesNotThrow(() -> {
            stockReport.getReportTitle();
            stockReport.getReportType();
            stockReport.generateBody();
            stockReport.generateSummary();
        });
    }
}