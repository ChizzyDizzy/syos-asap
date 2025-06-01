package com.syos.domain.decorators;

import com.syos.domain.entities.*;
import com.syos.domain.valueobjects.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Decorator that enhances Bill with multiple visitor support
 * Allows processing a bill with multiple visitors at once
 */
public class VisitableBill implements BillInterface {
    private final Bill bill;
    private final List<BillVisitor> visitorsHistory;

    public VisitableBill(Bill bill) {
        if (bill == null) {
            throw new IllegalArgumentException("Bill cannot be null");
        }
        this.bill = bill;
        this.visitorsHistory = new ArrayList<>();
    }

    // Delegate all BillInterface methods to the wrapped bill
    @Override
    public BillNumber getBillNumber() {
        return bill.getBillNumber();
    }

    @Override
    public LocalDateTime getBillDate() {
        return bill.getBillDate();
    }

    @Override
    public List<BillItem> getItems() {
        return bill.getItems();
    }

    @Override
    public Money getTotalAmount() {
        return bill.getTotalAmount();
    }

    @Override
    public Money getDiscount() {
        return bill.getDiscount();
    }

    @Override
    public Money getCashTendered() {
        return bill.getCashTendered();
    }

    @Override
    public Money getChange() {
        return bill.getChange();
    }

    @Override
    public TransactionType getTransactionType() {
        return bill.getTransactionType();
    }

    @Override
    public Money getFinalAmount() {
        return bill.getFinalAmount();
    }

    @Override
    public void accept(BillVisitor visitor) {
        visitor.visit(this.getOriginalBill());
        visitorsHistory.add(visitor);
    }

    // Enhanced functionality - accept multiple visitors
    public void accept(List<BillVisitor> visitors) {
        for (BillVisitor visitor : visitors) {
            accept(visitor);
        }
    }

    // Accept visitors with a specific order
    public void acceptInOrder(BillVisitor... visitors) {
        for (BillVisitor visitor : visitors) {
            accept(visitor);
        }
    }

    // Process with visitor and return result
    public <T> T processWithVisitor(ResultVisitor<T> visitor) {
        visitor.visit(this.getOriginalBill());
        return visitor.getResult();
    }

    // Get history of visitors that have processed this bill
    public List<BillVisitor> getVisitorsHistory() {
        return new ArrayList<>(visitorsHistory);
    }

    // Clear visitor history
    public void clearVisitorHistory() {
        visitorsHistory.clear();
    }

    // Get the original bill
    public Bill getOriginalBill() {
        return bill;
    }

    // Utility method to check if a specific visitor type has visited
    public boolean hasBeenVisitedBy(Class<? extends BillVisitor> visitorClass) {
        return visitorsHistory.stream()
                .anyMatch(visitor -> visitorClass.isInstance(visitor));
    }

    // Interface for visitors that return results
    public interface ResultVisitor<T> extends BillVisitor {
        T getResult();
    }
}