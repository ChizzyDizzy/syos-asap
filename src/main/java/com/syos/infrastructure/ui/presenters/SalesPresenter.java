package com.syos.infrastructure.ui.presenters;

import com.syos.domain.entities.Bill;
import com.syos.domain.valueobjects.Money;
import com.syos.application.visitors.BillPrinter;

public class SalesPresenter {

    public void showSaleHeader() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("New Sale Transaction");
        System.out.println("=".repeat(50));
    }

    public void showItemAdded(String itemCode, int quantity) {
        System.out.println("✓ Added: " + itemCode + " x " + quantity);
    }

    public void showSubtotal(Money subtotal) {
        System.out.println("\nSubtotal: " + subtotal);
    }

    public void showBill(Bill bill) {
        BillPrinter printer = new BillPrinter();
        bill.accept(printer);
        System.out.println(printer.getOutput());
    }

    public void showError(String message) {
        System.err.println("❌ Error: " + message);
    }
}