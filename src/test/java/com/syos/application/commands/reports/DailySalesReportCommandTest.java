package com.syos.application.commands.reports;

import com.syos.application.services.ReportService;
import com.syos.infrastructure.ui.presenters.ReportPresenter;
import com.syos.infrastructure.ui.cli.InputReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

/**
 * Essential Clean Unit Tests for DailySalesReportCommand
 * 3 focused tests covering core functionality, edge cases, and design patterns
 */
@DisplayName("SYOS DailySalesReportCommand - Essential Tests")
class DailySalesReportCommandTest {

    @Mock private ReportService reportService;
    @Mock private ReportPresenter presenter;
    @Mock private InputReader inputReader;

    private DailySalesReportCommand dailySalesReportCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dailySalesReportCommand = new DailySalesReportCommand(reportService, presenter, inputReader);
    }

    @Test
    @DisplayName("Should successfully generate and display daily sales report for specified date")
    void should_generate_daily_sales_report_for_specified_date() {
        // Arrange - SYOS employee requests report for specific date
        LocalDate reportDate = LocalDate.of(2024, 6, 15);
        String mockReport = "Daily Sales Report for 2024-06-15\nTotal Sales: LKR 25,750.00";

        when(inputReader.readDate("Enter date (YYYY-MM-DD) or press Enter for today: ")).thenReturn(reportDate);
        when(reportService.generateDailySalesReport(reportDate)).thenReturn(mockReport);

        // Act
        dailySalesReportCommand.execute();

        // Assert - Verify complete workflow
        verify(inputReader).readDate("Enter date (YYYY-MM-DD) or press Enter for today: ");
        verify(reportService).generateDailySalesReport(reportDate);
        verify(presenter).showReport(mockReport);
        verify(presenter, never()).showError(anyString());
    }

    @Test
    @DisplayName("Should handle null date input and exception scenarios gracefully")
    void should_handle_null_date_and_exceptions() {
        // Arrange - User presses Enter (null date) and service throws exception
        when(inputReader.readDate("Enter date (YYYY-MM-DD) or press Enter for today: ")).thenReturn(null);
        when(reportService.generateDailySalesReport(any(LocalDate.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act
        dailySalesReportCommand.execute();

        // Assert - Verify default to today's date and error handling
        verify(reportService).generateDailySalesReport(LocalDate.now());
        verify(presenter).showError("Failed to generate report: Database connection failed");
        verify(presenter, never()).showReport(anyString());
    }

    @Test
    @DisplayName("Should implement Command pattern correctly with proper description")
    void should_implement_command_pattern_correctly() {
        // Act & Assert - Verify Command pattern implementation
        assertEquals("Daily Sales Report", dailySalesReportCommand.getDescription());
        assertTrue(dailySalesReportCommand instanceof com.syos.application.interfaces.Command);

        // Verify command is executable without errors
        assertDoesNotThrow(() -> {
            when(inputReader.readDate(anyString())).thenReturn(LocalDate.now());
            when(reportService.generateDailySalesReport(any())).thenReturn("Test Report");
            dailySalesReportCommand.execute();
        });
    }
}