package com.syos.application.commands.reports;

import com.syos.application.services.ReportService;
import com.syos.infrastructure.ui.presenters.ReportPresenter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Essential Clean Unit Tests for ReorderReportCommand
 * 3 focused tests covering core functionality, edge cases, and design patterns
 */
@DisplayName("SYOS ReorderReportCommand - Essential Tests")
class ReorderReportCommandTest {

    @Mock private ReportService reportService;
    @Mock private ReportPresenter presenter;

    private ReorderReportCommand reorderReportCommand;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream standardOut = System.out;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reorderReportCommand = new ReorderReportCommand(reportService, presenter);
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    @DisplayName("Should successfully generate reorder report and display options menu")
    void should_generate_reorder_report_and_show_options() {
        // Arrange - SYOS reorder report generation
        String mockReport = "REORDER REPORT\nMILK001 - Highland Milk: Current: 15, Suggested: 85";
        when(reportService.generateReorderReport()).thenReturn(mockReport);

        // Act
        reorderReportCommand.execute();

        // Assert - Verify complete workflow
        verify(presenter).showInfo("Generating Reorder Level Report...");
        verify(reportService).generateReorderReport();
        verify(presenter).showReport(mockReport);
        verify(presenter, never()).showError(anyString());

        // Verify reorder options are displayed
        String consoleOutput = outputStreamCaptor.toString();
        assertTrue(consoleOutput.contains("Reorder Actions:"));
        assertTrue(consoleOutput.contains("1. Generate purchase orders (future feature)"));
        assertTrue(consoleOutput.contains("2. Email suppliers (future feature)"));
        assertTrue(consoleOutput.contains("3. Export to CSV (future feature)"));
    }

    @Test
    @DisplayName("Should handle report service exceptions gracefully")
    void should_handle_report_service_exceptions() {
        // Arrange - Report service failure
        when(reportService.generateReorderReport())
                .thenThrow(new RuntimeException("Database connection timeout"));

        // Act
        reorderReportCommand.execute();

        // Assert - Verify error handling
        verify(presenter).showInfo("Generating Reorder Level Report...");
        verify(reportService).generateReorderReport();
        verify(presenter).showError("Failed to generate reorder report: Database connection timeout");
        verify(presenter, never()).showReport(anyString());

        // Verify options menu is not shown on error
        String consoleOutput = outputStreamCaptor.toString();
        assertFalse(consoleOutput.contains("Reorder Actions:"));
    }

    @Test
    @DisplayName("Should implement Command pattern correctly with proper description")
    void should_implement_command_pattern_correctly() {
        // Act & Assert - Verify Command pattern implementation
        assertEquals("Reorder Level Report", reorderReportCommand.getDescription());
        assertTrue(reorderReportCommand instanceof com.syos.application.interfaces.Command);

        // Verify command is executable without errors
        assertDoesNotThrow(() -> {
            when(reportService.generateReorderReport()).thenReturn("Test Report");
            reorderReportCommand.execute();
        });
    }
}