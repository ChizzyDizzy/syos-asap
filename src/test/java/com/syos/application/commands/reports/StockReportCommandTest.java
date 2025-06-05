package com.syos.application.commands.reports;

import com.syos.application.interfaces.Command;
import com.syos.application.services.ReportService;
import com.syos.application.services.InventoryService;
import com.syos.infrastructure.ui.presenters.ReportPresenter;
import com.syos.infrastructure.ui.presenters.InventoryPresenter;
import com.syos.infrastructure.ui.cli.InputReader;
import com.syos.domain.entities.Item;
import com.syos.domain.entities.*;
import com.syos.domain.valueobjects.*;
import com.syos.domain.interfaces.ItemState;
import com.syos.application.services.InventoryService.InventoryStatistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StockReportCommand following clean testing principles.
 *
 * Design Philosophy:
 * - London (Mockist) approach for fast, isolated testing
 * - F.I.R.S.T principles and 4 pillars of good unit tests
 * - Testing behaviors, not implementation details
 * - Command Pattern, Facade Pattern, and Builder Pattern validation
 */
@DisplayName("Stock Report Command Tests")
class StockReportCommandTest {

    @Mock private ReportService mockReportService;
    @Mock private InventoryService mockInventoryService;
    @Mock private ReportPresenter mockReportPresenter;
    @Mock private InventoryPresenter mockInventoryPresenter;
    @Mock private InputReader mockInputReader;

    private StockReportCommand stockReportCommand;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private AutoCloseable mockitoCloseable;

    private final String SAMPLE_STOCK_REPORT = "SYOS Stock Report\n=================\nSample stock data";

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);

        // Capture System.out for testing console output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        stockReportCommand = new StockReportCommand(
                mockReportService,
                mockInventoryService,
                mockReportPresenter,
                mockInventoryPresenter,
                mockInputReader
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
        mockitoCloseable.close();
    }

    @Test
    @DisplayName("Should execute complete report generation workflow successfully")
    void should_ExecuteCompleteWorkflow_when_ExecuteMethodCalled() {
        // Arrange
        when(mockReportService.generateStockReport()).thenReturn(SAMPLE_STOCK_REPORT);
        when(mockInputReader.readString("Select an option (0 to exit): ")).thenReturn("0");

        // Act
        stockReportCommand.execute();

        // Assert - Verify Command Pattern implementation and workflow
        var inOrder = inOrder(mockReportPresenter, mockReportService, mockInputReader);
        inOrder.verify(mockReportPresenter).showInfo("Generating Stock Report...");
        inOrder.verify(mockReportService).generateStockReport();
        inOrder.verify(mockReportPresenter).showReport(SAMPLE_STOCK_REPORT);
        inOrder.verify(mockInputReader).readString("Select an option (0 to exit): ");

        // Verify Command interface compliance
        assertInstanceOf(Command.class, stockReportCommand);
        assertEquals("Current Stock Report", stockReportCommand.getDescription());
    }

    @Test
    @DisplayName("Should generate detailed category report with inventory data")
    void should_GenerateDetailedCategoryReport_when_Option1Selected() {
        // Arrange
        List<Item> testItems = createTestInventoryItems();
        when(mockReportService.generateStockReport()).thenReturn(SAMPLE_STOCK_REPORT);
        when(mockInputReader.readString("Select an option (0 to exit): "))
                .thenReturn("1") // Category report
                .thenReturn("0"); // Exit
        when(mockInputReader.readString("\nPress Enter to continue...")).thenReturn("");
        when(mockInventoryService.getAllItems()).thenReturn(testItems);

        // Act
        stockReportCommand.execute();

        // Assert - Verify Facade Pattern usage and data processing
        verify(mockInventoryService).getAllItems();
        verify(mockReportPresenter).showInfo("Generating Detailed Category Report...");
        verify(mockInputReader).readString("\nPress Enter to continue...");

        String output = outputStream.toString();
        assertAll(
                "Category report should display properly formatted content",
                () -> assertTrue(output.contains("DETAILED STOCK REPORT BY CATEGORY")),
                () -> assertTrue(output.contains("Category: MILK")),
                () -> assertTrue(output.contains("Total Items:")),
                () -> assertTrue(output.contains("SUMMARY BY STATE"))
        );
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully with proper error messages")
    void should_HandleServiceException_when_ReportServiceFails() {
        // Arrange
        RuntimeException serviceException = new RuntimeException("Database connection failed");
        when(mockReportService.generateStockReport()).thenThrow(serviceException);

        // Act
        stockReportCommand.execute();

        // Assert - Verify error handling and resilience
        verify(mockReportPresenter).showInfo("Generating Stock Report...");
        verify(mockReportPresenter).showError("Failed to generate stock report: Database connection failed");
        // Should not proceed to menu options when exception occurs
        verify(mockInputReader, never()).readString(anyString());

        // Verify system doesn't crash and handles exceptions gracefully
        assertDoesNotThrow(() -> stockReportCommand.execute());
    }

    // Helper method following Test Data Builder pattern
    private List<Item> createTestInventoryItems() {
        List<Item> items = new ArrayList<>();

        // Create items using Builder pattern (demonstrating design pattern usage)
        Item milkItem = new Item.Builder()
                .withCode("MILK001")
                .withName("Fresh Milk 1L")
                .withPrice(BigDecimal.valueOf(3.50))
                .withQuantity(75)
                .withExpiryDate(LocalDate.now().plusDays(5))
                .withState(new OnShelfState())
                .build();

        Item breadItem = new Item.Builder()
                .withCode("BREAD001")
                .withName("Whole Grain Bread")
                .withPrice(BigDecimal.valueOf(2.25))
                .withQuantity(120)
                .withExpiryDate(LocalDate.now().plusDays(3))
                .withState(new OnShelfState())
                .build();

        items.add(milkItem);
        items.add(breadItem);

        return items;
    }
}