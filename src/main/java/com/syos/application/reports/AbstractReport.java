package com.syos.application.reports;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class AbstractReport {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Template Method
    public final String generateReport() {
        StringBuilder report = new StringBuilder();

        // Step 1: Add header
        report.append(generateHeader());
        report.append("\n");

        // Step 2: Add report metadata
        report.append(generateMetadata());
        report.append("\n");

        // Step 3: Add report body
        report.append(generateBody());
        report.append("\n");

        // Step 4: Add summary
        report.append(generateSummary());
        report.append("\n");

        // Step 5: Add footer
        report.append(generateFooter());

        return report.toString();
    }

    // Common implementation
    protected String generateHeader() {
        return "=".repeat(80) + "\n" +
                centerText(getReportTitle(), 80) + "\n" +
                "=".repeat(80);
    }

    protected String generateMetadata() {
        return "Generated: " + LocalDateTime.now().format(formatter) + "\n" +
                "Report Type: " + getReportType();
    }

    protected String generateFooter() {
        return "-".repeat(80) + "\n" +
                "End of Report";
    }

    // Abstract methods to be implemented by subclasses
    protected abstract String getReportTitle();
    protected abstract String getReportType();
    protected abstract String generateBody();
    protected abstract String generateSummary();

    // Utility method
    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text;
    }
}
