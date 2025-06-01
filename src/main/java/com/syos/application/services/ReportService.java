package com.syos.application.services;

import com.syos.application.reports.*;
import com.syos.domain.entities.*;
import com.syos.infrastructure.persistence.gateways.*;
import java.time.LocalDate;
import java.util.List;

public class ReportService {
    private final BillGateway billGateway;
    private final ItemGateway itemGateway;
    private static final int REORDER_THRESHOLD = 50;

    public ReportService(BillGateway billGateway, ItemGateway itemGateway) {
        this.billGateway = billGateway;
        this.itemGateway = itemGateway;
    }

    public String generateDailySalesReport(LocalDate date) {
        List<Bill> bills = billGateway.findByDate(date);
        DailySalesReport report = new DailySalesReport(date, bills);
        return report.generateReport();
    }

    public String generateStockReport() {
        List<Item> items = itemGateway.findAll();
        StockReport report = new StockReport(items);
        return report.generateReport();
    }

    public String generateReorderReport() {
        List<Item> lowStockItems = itemGateway.findLowStock(REORDER_THRESHOLD);
        ReorderReport report = new ReorderReport(lowStockItems, REORDER_THRESHOLD);
        return report.generateReport();
    }

    public String generateReshelveReport() {
        List<Item> expiringItems = itemGateway.findExpiringSoon(7);
        ReshelveReport report = new ReshelveReport(expiringItems);
        return report.generateReport();
    }
}
