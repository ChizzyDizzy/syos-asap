package com.syos.application.visitors;

import com.syos.domain.entities.*;
import com.syos.domain.interfaces.BillVisitor;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class BillPrinter implements BillVisitor {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final StringBuilder output;

    public BillPrinter() {
        this.output = new StringBuilder();
    }

    @Override
    public void visit(Bill bill) {
        output.setLength(0); // Clear previous output

        // Header
        output.append("\n").append("=".repeat(60)).append("\n");
        output.append(centerText("SYOS OUTLET STORE", 60)).append("\n");
        output.append(centerText("SALES RECEIPT", 60)).append("\n");
        output.append("=".repeat(60)).append("\n");

        // Bill details
        output.append("Bill No: ").append(bill.getBillNumber()).append("\n");
        output.append("Date: ").append(bill.getBillDate().format(formatter)).append("\n");
        output.append("Transaction Type: ").append(bill.getTransactionType()).append("\n");
        output.append("-".repeat(60)).append("\n");

        // Items
        output.append(String.format("%-30s %10s %15s%n", "Item", "Qty", "Total"));
        output.append("-".repeat(60)).append("\n");

        for (BillItem item : bill.getItems()) {
            output.append(String.format("%-30s %10d %15s%n",
                    truncate(item.getItem().getName(), 30),
                    item.getQuantity().getValue(),
                    item.getTotalPrice()));
        }

        output.append("-".repeat(60)).append("\n");

        // Totals
        output.append(String.format("%-40s %15s%n", "Subtotal:", bill.getTotalAmount()));
        if (bill.getDiscount().getValue().compareTo(BigDecimal.ZERO) > 0) {
            output.append(String.format("%-40s %15s%n", "Discount:", bill.getDiscount()));
            output.append(String.format("%-40s %15s%n", "Total:", bill.getFinalAmount()));
        }
        output.append(String.format("%-40s %15s%n", "Cash Tendered:", bill.getCashTendered()));
        output.append(String.format("%-40s %15s%n", "Change:", bill.getChange()));

        output.append("=".repeat(60)).append("\n");
        output.append(centerText("Thank you for shopping with us!", 60)).append("\n");
        output.append("=".repeat(60)).append("\n");
    }

    public String getOutput() {
        return output.toString();
    }

    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text;
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
