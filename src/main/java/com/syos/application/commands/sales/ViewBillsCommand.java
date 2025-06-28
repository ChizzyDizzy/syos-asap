package com.syos.application.commands.sales;

import com.syos.application.interfaces.Command;
import com.syos.application.services.SalesService;
import com.syos.infrastructure.ui.presenters.SalesPresenter;
import com.syos.infrastructure.ui.cli.InputReader;
import com.syos.domain.entities.Bill;
import com.syos.application.visitors.BillPrinter;
import com.syos.application.visitors.BillStatisticsVisitor;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Command to view bills/transactions
 * Allows viewing all bills, today's bills, or bills from a specific date
 */
public class ViewBillsCommand implements Command {
    private final SalesService salesService;
    private final SalesPresenter presenter;
    private final InputReader inputReader;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ViewBillsCommand(SalesService salesService, SalesPresenter salesPresenter) {
        this.salesService = salesService;
        this.presenter = salesPresenter;
        this.inputReader = InputReader.getInstance();
    }

    @Override
    public void execute() {
        // Display header manually since SalesPresenter doesn't have showHeader
        System.out.println("\n" + "=".repeat(50));
        System.out.println("View Bills");
        System.out.println("=".repeat(50));

        try {
            // Ask user for date selection
            System.out.println("\nSelect an option:");
            System.out.println("1. View all bills");
            System.out.println("2. View today's bills");
            System.out.println("3. View bills for a specific date");
            System.out.println("0. Back to menu");

            int choice = inputReader.readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    viewAllBills();
                    break;
                case 2:
                    viewTodaysBills();
                    break;
                case 3:
                    viewBillsByDate();
                    break;
                case 0:
                    return;
                default:
                    presenter.showError("Invalid choice. Please try again.");
            }

            // After viewing bills, offer additional options
            if (choice >= 1 && choice <= 3) {
                offerAdditionalOptions();
            }

        } catch (Exception e) {
            presenter.showError("Failed to retrieve bills: " + e.getMessage());
        }
    }

    private void viewAllBills() {
        presenter.showInfo("Retrieving all bills from the system...");

        List<Bill> bills = salesService.getAllBills();

        if (bills.isEmpty()) {
            presenter.showInfo("No bills found in the system.");
            return;
        }

        // Sort bills by date (most recent first) if needed
        bills.sort((b1, b2) -> b2.getBillDate().compareTo(b1.getBillDate()));

        displayBills(bills, "All Bills");

        // Show additional summary
        showBillsSummaryByDate(bills);
    }

    private void viewTodaysBills() {
        LocalDate today = LocalDate.now();
        presenter.showInfo("Retrieving bills for today: " + today.format(DATE_FORMAT));

        List<Bill> bills = salesService.getBillsForToday();
        displayBills(bills, "Today's Bills");
    }

    private void viewBillsByDate() {
        LocalDate date = inputReader.readDate("Enter date (YYYY-MM-DD): ");
        if (date == null) {
            presenter.showError("Invalid date format.");
            return;
        }

        presenter.showInfo("Retrieving bills for: " + date.format(DATE_FORMAT));

        List<Bill> bills = salesService.getBillsByDate(date);
        displayBills(bills, "Bills for " + date.format(DATE_FORMAT));
    }

    private void displayBills(List<Bill> bills, String title) {
        if (bills.isEmpty()) {
            presenter.showInfo("No bills found for the selected criteria.");
            return;
        }

        System.out.println("\n" + "=".repeat(100));
        System.out.println(title);
        System.out.println("=".repeat(100));
        System.out.println(String.format("%-20s %-20s %10s %12s %15s %15s %12s",
                "Bill Number", "Date/Time", "Type", "Items", "Total", "Cash", "Change"));
        System.out.println("-".repeat(100));

        // Display summary for each bill
        for (Bill bill : bills) {
            System.out.println(String.format("%-20s %-20s %10s %12d %15s %15s %12s",
                    bill.getBillNumber(),
                    bill.getBillDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    bill.getTransactionType(),
                    bill.getTotalItemCount(),
                    bill.getTotalAmount(),
                    bill.getCashTendered(),
                    bill.getChange()
            ));
        }
        System.out.println("-".repeat(100));

        // Show statistics
        showBillStatistics(bills);
    }

    private void showBillStatistics(List<Bill> bills) {
        BillStatisticsVisitor statsVisitor = new BillStatisticsVisitor();

        // Visit all bills to gather statistics
        for (Bill bill : bills) {
            bill.accept(statsVisitor);
        }

        System.out.println("\n" + "=".repeat(50));
        System.out.println("Sales Statistics");
        System.out.println("=".repeat(50));
        System.out.println("Total Bills: " + statsVisitor.getBillCount());
        System.out.println("Total Revenue: " + statsVisitor.getTotalRevenue());
        System.out.println("Average Transaction: " + statsVisitor.getAverageTransaction());
        System.out.println("Total Discount Given: " + statsVisitor.getTotalDiscount());
        System.out.println("Most Popular Item: " + statsVisitor.getMostPopularItem());
        System.out.println("=".repeat(50));
    }

    private void showBillsSummaryByDate(List<Bill> bills) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Daily Summary");
        System.out.println("=".repeat(50));

        // Group bills by date
        java.util.Map<LocalDate, List<Bill>> billsByDate = new java.util.HashMap<>();
        for (Bill bill : bills) {
            LocalDate date = bill.getBillDate().toLocalDate();
            billsByDate.computeIfAbsent(date, k -> new java.util.ArrayList<>()).add(bill);
        }

        // Sort dates and display summary
        billsByDate.keySet().stream()
                .sorted(java.util.Comparator.reverseOrder())
                .forEach(date -> {
                    List<Bill> dailyBills = billsByDate.get(date);
                    double dailyTotal = dailyBills.stream()
                            .mapToDouble(b -> b.getFinalAmount().getValue().doubleValue())
                            .sum();

                    System.out.printf("%s: %d bills, Total: $%.2f%n",
                            date.format(DATE_FORMAT),
                            dailyBills.size(),
                            dailyTotal);
                });

        System.out.println("=".repeat(50));
    }

    private void offerAdditionalOptions() {
        while (true) {
            System.out.println("\nAdditional Options:");
            System.out.println("1. View detailed bill");
            System.out.println("2. Search bill by number");
            System.out.println("3. Filter by transaction type");
            System.out.println("0. Back to main menu");

            int choice = inputReader.readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    viewDetailedBill();
                    break;
                case 2:
                    searchBillByNumber();
                    break;
                case 3:
                    filterByTransactionType();
                    break;
                case 0:
                    return;
                default:
                    presenter.showError("Invalid choice.");
            }
        }
    }

    private void viewDetailedBill() {
        String billNumberStr = inputReader.readString("Enter bill number (e.g., BILL-000001): ");

        try {
            // Extract the numeric part from the bill number
            int billNumber = Integer.parseInt(billNumberStr.replace("BILL-", ""));

            Bill bill = salesService.getBillByNumber(billNumber);

            if (bill != null) {
                presenter.showBill(bill);  // Use existing showBill method
            } else {
                presenter.showError("Bill not found: " + billNumberStr);
            }
        } catch (NumberFormatException e) {
            presenter.showError("Invalid bill number format. Use format: BILL-000001");
        }
    }

    private void searchBillByNumber() {
        String searchTerm = inputReader.readString("Enter partial bill number to search: ");

        presenter.showInfo("Searching for bills containing: " + searchTerm);

        List<Bill> allBills = salesService.getAllBills();
        List<Bill> matchingBills = allBills.stream()
                .filter(bill -> bill.getBillNumber().toString().contains(searchTerm))
                .collect(java.util.stream.Collectors.toList());

        if (matchingBills.isEmpty()) {
            presenter.showInfo("No bills found matching: " + searchTerm);
        } else {
            displayBills(matchingBills, "Search Results for: " + searchTerm);
        }
    }

    private void filterByTransactionType() {
        System.out.println("\nSelect transaction type:");
        System.out.println("1. In-Store");
        System.out.println("2. Online");

        int choice = inputReader.readInt("Enter choice: ");

        String type = choice == 1 ? "IN_STORE" : "ONLINE";

        List<Bill> allBills = salesService.getAllBills();
        List<Bill> filteredBills = allBills.stream()
                .filter(bill -> bill.getTransactionType().name().equals(type))
                .collect(java.util.stream.Collectors.toList());

        displayBills(filteredBills, type + " Transactions");
    }

    @Override
    public String getDescription() {
        return "View Bills and Transactions";
    }
}