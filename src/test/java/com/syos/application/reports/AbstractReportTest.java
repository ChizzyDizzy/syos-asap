package com.syos.application.reports;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AbstractReport following clean testing principles.
 *
 * Design Philosophy:
 * - Tests Template Method Pattern implementation
 * - Validates algorithm structure and extensibility
 * - Uses concrete test implementation to test abstract behavior
 * - F.I.R.S.T principles and clean testing practices
 */
@DisplayName("Abstract Report Tests")
class AbstractReportTest {

    private TestableReport testableReport;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        testableReport = new TestableReport();
    }

    @Test
    @DisplayName("Should execute complete Template Method workflow in correct order")
    void should_ExecuteTemplateMethodWorkflow_when_GenerateReportCalled() {
        // Act
        String generatedReport = testableReport.generateReport();

        // Assert - Verify Template Method Pattern implementation
        assertAll(
                "Template Method should coordinate all report sections",
                () -> assertNotNull(generatedReport, "Report should not be null"),
                () -> assertTrue(generatedReport.contains("TEST STOCK REPORT"), "Should contain report title"),
                () -> assertTrue(generatedReport.contains("Generated: "), "Should contain metadata"),
                () -> assertTrue(generatedReport.contains("Report Type: Stock Analysis"), "Should contain report type"),
                () -> assertTrue(generatedReport.contains("Sample report body content"), "Should contain body"),
                () -> assertTrue(generatedReport.contains("Total items processed: 100"), "Should contain summary"),
                () -> assertTrue(generatedReport.contains("End of Report"), "Should contain footer"),
                () -> assertTrue(generatedReport.contains("=".repeat(80)), "Should contain proper formatting")
        );

        // Verify the order of sections (Template Method algorithm structure)
        int headerIndex = generatedReport.indexOf("TEST STOCK REPORT");
        int metadataIndex = generatedReport.indexOf("Generated: ");
        int bodyIndex = generatedReport.indexOf("Sample report body content");
        int summaryIndex = generatedReport.indexOf("Total items processed: 100");
        int footerIndex = generatedReport.indexOf("End of Report");

        assertAll(
                "Template Method should maintain correct section order",
                () -> assertTrue(headerIndex < metadataIndex, "Header should come before metadata"),
                () -> assertTrue(metadataIndex < bodyIndex, "Metadata should come before body"),
                () -> assertTrue(bodyIndex < summaryIndex, "Body should come before summary"),
                () -> assertTrue(summaryIndex < footerIndex, "Summary should come before footer")
        );
    }

    @Test
    @DisplayName("Should coordinate abstract methods with template algorithm correctly")
    void should_CoordinateAbstractMethods_when_TemplateMethodExecuted() {
        // Arrange
        SpyableReport spyReport = new SpyableReport();

        // Act
        String report = spyReport.generateReport();

        // Assert - Verify abstract method coordination
        assertAll(
                "Template Method should call all abstract methods",
                () -> assertTrue(spyReport.wasGetReportTitleCalled(), "getReportTitle() should be called"),
                () -> assertTrue(spyReport.wasGetReportTypeCalled(), "getReportType() should be called"),
                () -> assertTrue(spyReport.wasGenerateBodyCalled(), "generateBody() should be called"),
                () -> assertTrue(spyReport.wasGenerateSummaryCalled(), "generateSummary() should be called")
        );

        // Verify metadata contains current timestamp (within reasonable range)
        assertTrue(report.contains("Generated: "), "Should contain timestamp");
        assertTrue(report.contains("Report Type: "), "Should contain report type");

        // Verify final method cannot be overridden (Template Method Pattern characteristic)
        assertDoesNotThrow(() -> spyReport.generateReport(),
                "Final template method should be stable and not overrideable");
    }

    /**
     * Concrete test implementation of AbstractReport for testing purposes
     * Follows Test Double pattern for testing abstract classes
     */
    private static class TestableReport extends AbstractReport {
        @Override
        protected String getReportTitle() {
            return "TEST STOCK REPORT";
        }

        @Override
        protected String getReportType() {
            return "Stock Analysis";
        }

        @Override
        protected String generateBody() {
            return "Sample report body content\nWith multiple lines\nAnd detailed information";
        }

        @Override
        protected String generateSummary() {
            return "Total items processed: 100\nTotal value: $5,000.00";
        }
    }

    /**
     * Spy implementation to verify method call coordination
     * Demonstrates testing abstract method integration
     */
    private static class SpyableReport extends AbstractReport {
        private boolean getReportTitleCalled = false;
        private boolean getReportTypeCalled = false;
        private boolean generateBodyCalled = false;
        private boolean generateSummaryCalled = false;

        @Override
        protected String getReportTitle() {
            getReportTitleCalled = true;
            return "SPY REPORT";
        }

        @Override
        protected String getReportType() {
            getReportTypeCalled = true;
            return "Test Report";
        }

        @Override
        protected String generateBody() {
            generateBodyCalled = true;
            return "Spy body content";
        }

        @Override
        protected String generateSummary() {
            generateSummaryCalled = true;
            return "Spy summary";
        }

        // Spy methods for verification
        boolean wasGetReportTitleCalled() { return getReportTitleCalled; }
        boolean wasGetReportTypeCalled() { return getReportTypeCalled; }
        boolean wasGenerateBodyCalled() { return generateBodyCalled; }
        boolean wasGenerateSummaryCalled() { return generateSummaryCalled; }
    }
}