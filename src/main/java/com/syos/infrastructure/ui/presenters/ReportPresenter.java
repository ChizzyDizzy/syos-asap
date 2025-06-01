package com.syos.infrastructure.ui.presenters;

/**
 * Presenter for report-related UI operations
 * Handles the display of reports and report-related messages
 */
public class ReportPresenter {

    /**
     * Display a report
     * @param report The report content to display
     */
    public void showReport(String report) {
        System.out.println(report);
    }

    /**
     * Display an error message
     * @param message The error message to display
     */
    public void showError(String message) {
        System.err.println("❌ Error: " + message);
    }

    /**
     * Display an informational message
     * @param message The info message to display
     */
    public void showInfo(String message) {
        System.out.println("ℹ️  " + message);
    }

    /**
     * Display a success message
     * @param message The success message to display
     */
    public void showSuccess(String message) {
        System.out.println("✅ " + message);
    }

    /**
     * Display a warning message
     * @param message The warning message to display
     */
    public void showWarning(String message) {
        System.out.println("⚠️  " + message);
    }

    /**
     * Display a header for a section
     * @param title The title to display as header
     */
    public void showHeader(String title) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(title);
        System.out.println("=".repeat(50));
    }

    /**
     * Display a separator line
     */
    public void showSeparator() {
        System.out.println("-".repeat(50));
    }

    /**
     * Clear the screen (platform-dependent)
     */
    public void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // If clearing fails, just add some blank lines
            System.out.println("\n".repeat(50));
        }
    }

    /**
     * Display a progress indicator
     * @param message The progress message
     */
    public void showProgress(String message) {
        System.out.print(message + "... ");
    }

    /**
     * Complete a progress indicator
     */
    public void showProgressComplete() {
        System.out.println("Done ✓");
    }

    /**
     * Display a formatted table header
     * @param columns The column headers
     * @param widths The width for each column
     */
    public void showTableHeader(String[] columns, int[] widths) {
        if (columns.length != widths.length) {
            throw new IllegalArgumentException("Columns and widths must have the same length");
        }

        StringBuilder format = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            format.append("%-").append(widths[i]).append("s ");
        }
        format.append("%n");

        System.out.printf(format.toString(), (Object[]) columns);

        // Print separator line
        int totalWidth = 0;
        for (int width : widths) {
            totalWidth += width + 1;
        }
        System.out.println("-".repeat(totalWidth));
    }
}