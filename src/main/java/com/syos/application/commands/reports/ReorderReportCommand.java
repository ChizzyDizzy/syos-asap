package com.syos.application.commands.reports;

import com.syos.application.interfaces.Command;
import com.syos.application.services.ReportService;
import com.syos.infrastructure.ui.presenters.ReportPresenter;

/**
 * Command to generate reorder level report
 * Shows items that need to be reordered (quantity < 50)
 */
public class ReorderReportCommand implements Command {
    private final ReportService reportService;
    private final ReportPresenter presenter;

    public ReorderReportCommand(ReportService reportService, ReportPresenter presenter) {
        this.reportService = reportService;
        this.presenter = presenter;
    }

    @Override
    public void execute() {
        try {
            presenter.showInfo("Generating Reorder Level Report...");

            // Generate the report
            String report = reportService.generateReorderReport();

            // Display the report
            presenter.showReport(report);

            // Show additional options
            showReorderOptions();

        } catch (Exception e) {
            presenter.showError("Failed to generate reorder report: " + e.getMessage());
        }
    }

    private void showReorderOptions() {
        System.out.println("\nReorder Actions:");
        System.out.println("1. Generate purchase orders (future feature)");
        System.out.println("2. Email suppliers (future feature)");
        System.out.println("3. Export to CSV (future feature)");
        System.out.println("\nPress Enter to continue...");

        try {
            System.in.read();
        } catch (Exception e) {
            // Ignore
        }
    }

    @Override
    public String getDescription() {
        return "Reorder Level Report";
    }
}