package com.syos.application.commands.reports;

import com.syos.application.interfaces.Command;
import com.syos.application.services.ReportService;
import com.syos.application.services.InventoryService;
import com.syos.infrastructure.ui.presenters.ReportPresenter;
import com.syos.infrastructure.ui.presenters.InventoryPresenter;
import com.syos.infrastructure.ui.cli.InputReader;
import com.syos.domain.entities.Item;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.awt.Desktop;

/**
 * Command to generate and display stock report with additional options
 * Shows current inventory status grouped by item state
 */
public class StockReportCommand implements Command {
    private final ReportService reportService;
    private final InventoryService inventoryService;
    private final ReportPresenter presenter;
    private final InventoryPresenter inventoryPresenter;
    private final InputReader inputReader;
    private String lastGeneratedReport;

    public StockReportCommand(ReportService reportService,
                              InventoryService inventoryService,
                              ReportPresenter presenter,
                              InventoryPresenter inventoryPresenter,
                              InputReader inputReader) {
        this.reportService = reportService;
        this.inventoryService = inventoryService;
        this.presenter = presenter;
        this.inventoryPresenter = inventoryPresenter;
        this.inputReader = InputReader.getInstance();
    }

    @Override
    public void execute() {
        try {
            presenter.showInfo("Generating Stock Report...");

            // Generate the main report
            lastGeneratedReport = reportService.generateStockReport();
            presenter.showReport(lastGeneratedReport);

            // Offer additional options
            handleReportOptions();

        } catch (Exception e) {
            presenter.showError("Failed to generate stock report: " + e.getMessage());
        }
    }

    private void handleReportOptions() {
        boolean continueOptions = true;

        while (continueOptions) {
            displayOptions();

            String choice = inputReader.readString("Select an option (0 to exit): ");

            switch (choice) {
                case "1":
                    generateDetailedCategoryReport();
                    break;
                case "2":
                    showLowStockItems();
                    break;
                case "3":
                    showExpiringItems();
                    break;
                case "4":
                    exportToFile();
                    break;
                case "0":
                    continueOptions = false;
                    break;
                default:
                    presenter.showError("Invalid option. Please try again.");
            }

            if (continueOptions && !choice.equals("0")) {
                inputReader.readString("\nPress Enter to continue...");
            }
        }
    }

    private void displayOptions() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Stock Report Options");
        System.out.println("=".repeat(50));
        System.out.println("1. Generate detailed report by category");
        System.out.println("2. Show only low stock items");
        System.out.println("3. Show expiring items");
        System.out.println("4. Export to file");
        System.out.println("0. Back to main menu");
        System.out.println("=".repeat(50));
    }

    private void generateDetailedCategoryReport() {
        try {
            presenter.showInfo("Generating Detailed Category Report...");

            List<Item> allItems = inventoryService.getAllItems();

            // Group items by state
            Map<String, List<Item>> itemsByState = allItems.stream()
                    .collect(Collectors.groupingBy(item -> item.getState().getStateName()));

            // Group items by category (using first part of code)
            Map<String, List<Item>> itemsByCategory = allItems.stream()
                    .collect(Collectors.groupingBy(item -> {
                        String code = item.getCode().getValue();
                        // Extract category from code (e.g., MILK001 -> MILK)
                        return code.replaceAll("[0-9]", "");
                    }));

            // Display detailed report
            System.out.println("\n" + "=".repeat(80));
            System.out.println("DETAILED STOCK REPORT BY CATEGORY");
            System.out.println("=".repeat(80));

            for (Map.Entry<String, List<Item>> entry : itemsByCategory.entrySet()) {
                String category = entry.getKey();
                List<Item> items = entry.getValue();

                System.out.println("\nCategory: " + category);
                System.out.println("-".repeat(60));

                int totalQuantity = items.stream()
                        .mapToInt(item -> item.getQuantity().getValue())
                        .sum();

                double totalValue = items.stream()
                        .mapToDouble(item -> item.getPrice().getValue().doubleValue() * item.getQuantity().getValue())
                        .sum();

                System.out.printf("Total Items: %d | Total Quantity: %d | Total Value: $%.2f%n",
                        items.size(), totalQuantity, totalValue);

                System.out.println("\nItems:");
                System.out.printf("%-15s %-30s %10s %10s %12s%n",
                        "Code", "Name", "Quantity", "Price", "State");
                System.out.println("-".repeat(80));

                for (Item item : items) {
                    System.out.printf("%-15s %-30s %10d %10s %12s%n",
                            item.getCode().getValue(),
                            truncate(item.getName(), 30),
                            item.getQuantity().getValue(),
                            item.getPrice(),
                            item.getState().getStateName());
                }
            }

            // Summary by state
            System.out.println("\n" + "=".repeat(80));
            System.out.println("SUMMARY BY STATE");
            System.out.println("=".repeat(80));

            for (Map.Entry<String, List<Item>> entry : itemsByState.entrySet()) {
                String state = entry.getKey();
                int count = entry.getValue().size();
                int quantity = entry.getValue().stream()
                        .mapToInt(item -> item.getQuantity().getValue())
                        .sum();

                System.out.printf("%-15s: %d items (%d units)%n", state, count, quantity);
            }

        } catch (Exception e) {
            presenter.showError("Failed to generate category report: " + e.getMessage());
        }
    }

    private void showLowStockItems() {
        try {
            presenter.showInfo("Retrieving Low Stock Items...");

            List<Item> lowStockItems = inventoryService.getLowStockItems();

            if (lowStockItems.isEmpty()) {
                presenter.showSuccess("All items are adequately stocked!");
                return;
            }

            System.out.println("\n" + "=".repeat(80));
            System.out.println("LOW STOCK ITEMS (Quantity < 50)");
            System.out.println("=".repeat(80));
            System.out.printf("%-15s %-30s %10s %15s %15s%n",
                    "Code", "Name", "Quantity", "Reorder Qty", "Current Value");
            System.out.println("-".repeat(80));

            for (Item item : lowStockItems) {
                int reorderQty = 100 - item.getQuantity().getValue(); // Suggested reorder to reach 100
                double currentValue = item.getPrice().getValue().doubleValue() * item.getQuantity().getValue();

                System.out.printf("%-15s %-30s %10d %15d $%14.2f%n",
                        item.getCode().getValue(),
                        truncate(item.getName(), 30),
                        item.getQuantity().getValue(),
                        reorderQty,
                        currentValue);
            }

            System.out.println("-".repeat(80));
            System.out.println("Total items below reorder level: " + lowStockItems.size());

        } catch (Exception e) {
            presenter.showError("Failed to retrieve low stock items: " + e.getMessage());
        }
    }

    private void showExpiringItems() {
        try {
            int days = inputReader.readInt("Show items expiring within how many days? (default 7): ");
            if (days <= 0) days = 7;

            presenter.showInfo("Retrieving items expiring within " + days + " days...");

            List<Item> expiringItems = inventoryService.getExpiringItems(days);

            if (expiringItems.isEmpty()) {
                presenter.showSuccess("No items expiring within " + days + " days!");
                return;
            }

            System.out.println("\n" + "=".repeat(80));
            System.out.println("ITEMS EXPIRING WITHIN " + days + " DAYS");
            System.out.println("=".repeat(80));
            System.out.printf("%-15s %-30s %10s %15s %10s %12s%n",
                    "Code", "Name", "Quantity", "Expiry Date", "Days Left", "Action");
            System.out.println("-".repeat(80));

            for (Item item : expiringItems) {
                long daysLeft = item.daysUntilExpiry();
                String action = daysLeft <= 1 ? "REMOVE" :
                        daysLeft <= 3 ? "DISCOUNT" : "MONITOR";

                System.out.printf("%-15s %-30s %10d %15s %10d %12s%n",
                        item.getCode().getValue(),
                        truncate(item.getName(), 30),
                        item.getQuantity().getValue(),
                        item.getExpiryDate(),
                        daysLeft,
                        action);
            }

            System.out.println("-".repeat(80));

            // Group by urgency
            long critical = expiringItems.stream().filter(i -> i.daysUntilExpiry() <= 1).count();
            long urgent = expiringItems.stream().filter(i -> i.daysUntilExpiry() > 1 && i.daysUntilExpiry() <= 3).count();
            long warning = expiringItems.stream().filter(i -> i.daysUntilExpiry() > 3).count();

            System.out.println("\nSummary:");
            System.out.println("Critical (≤1 day): " + critical + " items");
            System.out.println("Urgent (2-3 days): " + urgent + " items");
            System.out.println("Warning (>3 days): " + warning + " items");

        } catch (Exception e) {
            presenter.showError("Failed to retrieve expiring items: " + e.getMessage());
        }
    }

///  Method to export the report to a file (this will place the report in a folder called reports/ inside the main src directory, and it would open up the folder after generating the report)
    private void exportToFile() {
        try {
            // Create reports directory if it doesn't exist
            File reportsDir = new File("reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdir();
            }

            String filename = inputReader.readString("Enter filename (without extension): ");
            if (filename.trim().isEmpty()) {
                filename = "stock_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            }

            // Save to reports directory
            File file = new File(reportsDir, filename + ".txt");
            String fullPath = file.getAbsolutePath();

            presenter.showInfo("Exporting to " + fullPath + "...");

            try (FileWriter writer = new FileWriter(file)) {
                writer.write("SYOS STOCK REPORT\n");
                writer.write("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
                writer.write("=".repeat(80) + "\n\n");

                // Write the main report
                if (lastGeneratedReport != null) {
                    writer.write(lastGeneratedReport);
                    writer.write("\n\n");
                }

                // Add inventory statistics
                var stats = inventoryService.getInventoryStatistics();
                writer.write("INVENTORY STATISTICS\n");
                writer.write("-".repeat(40) + "\n");
                writer.write("Total Item Types: " + stats.totalItems + "\n");
                writer.write("Total Quantity: " + stats.totalQuantity + "\n");
                writer.write("Total Value: $" + stats.totalValue + "\n");
                writer.write("Expired Items: " + stats.expiredCount + "\n");
                writer.write("Low Stock Items: " + stats.lowStockCount + "\n");
                writer.write("Expiring Soon: " + stats.expiringCount + "\n");

                presenter.showSuccess("Report exported successfully!");
                presenter.showInfo("File saved to: " + fullPath);

                // Offer to open the file location
                if (Desktop.isDesktopSupported()) {
                    if (inputReader.readBoolean("Open file location?")) {
                        Desktop.getDesktop().open(reportsDir);
                    }
                }
            }

        } catch (IOException e) {
            presenter.showError("Failed to export report: " + e.getMessage());
        }
    }


    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    @Override
    public String getDescription() {
        return "Current Stock Report";
    }
}