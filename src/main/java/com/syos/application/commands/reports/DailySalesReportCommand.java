package com.syos.application.commands.reports;

import com.syos.application.interfaces.Command;
import com.syos.application.services.ReportService;
import com.syos.infrastructure.ui.presenters.ReportPresenter;
import com.syos.infrastructure.ui.cli.InputReader;
import java.time.LocalDate;

public class DailySalesReportCommand implements Command {
    private final ReportService reportService;
    private final ReportPresenter presenter;
    private final InputReader inputReader;

    public DailySalesReportCommand(ReportService reportService,
                                   ReportPresenter presenter,
                                   InputReader inputReader) {
        this.reportService = reportService;
        this.presenter = presenter;
        this.inputReader = inputReader;
    }

/* <<<<<<<<<<<<<<  ✨ Windsurf Command ⭐ >>>>>>>>>>>>>>>> */
    /**
     * Generates a daily sales report for the given date. If the date is null (i.e. the user pressed enter),
     * the report will be generated for the current date.
     * @throws Exception if there is an error generating the report
     */
/* <<<<<<<<<<  19474839-513e-4e18-b702-4b0e985379a1  >>>>>>>>>>> */
    @Override
    public void execute() {
        try {
            LocalDate date = inputReader.readDate("Enter date (YYYY-MM-DD) or press Enter for today: ");
            if (date == null) {
                date = LocalDate.now();
            }

            var report = reportService.generateDailySalesReport(date);
            presenter.showReport(report);
        } catch (Exception e) {
            presenter.showError("Failed to generate report: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Daily Sales Report";
    }
}