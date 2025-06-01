package com.syos.application.reports;

import com.syos.domain.entities.Bill;
import com.syos.domain.entities.BillItem;
import com.syos.domain.valueobjects.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class DailySalesReport extends AbstractReport {
    private final LocalDate date;
    private final List<Bill> bills;
    private final Map<String, SalesData> salesByItem;

    public DailySalesReport(LocalDate date, List<Bill> bills) {
        this.date = date;
        this.bills = bills;
        this.salesByItem = aggregateSales();
    }

    @Override
    protected String getReportTitle() {
        return "Daily Sales Report - " + date;
    }

    @Override
    protected String getReportType() {
        return "DAILY_SALES";
    }

    @Override
    protected String generateBody() {
        StringBuilder body = new StringBuilder();
        body.append("\nSales Summary by Item:\n");
        body.append("-".repeat(80)).append("\n");
        body.append(String.format("%-15s %-30s %10s %15s%n",
                "Item Code", "Item Name", "Quantity", "Total Revenue"));
        body.append("-".repeat(80)).append("\n");

        for (Map.Entry<String, SalesData> entry : salesByItem.entrySet()) {
            SalesData data = entry.getValue();
            body.append(String.format("%-15s %-30s %10d %15s%n",
                    data.itemCode,
                    data.itemName,
                    data.totalQuantity,
                    data.totalRevenue));
        }

        return body.toString();
    }

    @Override
    protected String generateSummary() {
        int totalTransactions = bills.size();
        int totalItemsSold = salesByItem.values().stream()
                .mapToInt(data -> data.totalQuantity)
                .sum();
        Money totalRevenue = salesByItem.values().stream()
                .map(data -> data.totalRevenue)
                .reduce(new Money(BigDecimal.ZERO), Money::add);

        return "\nSummary:\n" +
                "Total Transactions: " + totalTransactions + "\n" +
                "Total Items Sold: " + totalItemsSold + "\n" +
                "Total Revenue: " + totalRevenue + "\n";
    }

    private Map<String, SalesData> aggregateSales() {
        Map<String, SalesData> aggregated = new HashMap<>();

        for (Bill bill : bills) {
            for (BillItem item : bill.getItems()) {
                String code = item.getItem().getCode().getValue();
                aggregated.computeIfAbsent(code, k -> new SalesData(
                        item.getItem().getCode().getValue(),
                        item.getItem().getName()
                )).addSale(item.getQuantity().getValue(), item.getTotalPrice());
            }
        }

        return aggregated;
    }

    private static class SalesData {
        final String itemCode;
        final String itemName;
        int totalQuantity;
        Money totalRevenue;

        SalesData(String itemCode, String itemName) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.totalQuantity = 0;
            this.totalRevenue = new Money(BigDecimal.ZERO);
        }

        void addSale(int quantity, Money revenue) {
            this.totalQuantity += quantity;
            this.totalRevenue = this.totalRevenue.add(revenue);
        }
    }
}
