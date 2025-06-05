package com.syos.domain.interfaces;

import com.syos.domain.entities.Bill;

/**
 * Visitor interface for Bill entities
 * Supports visiting different types of bills
 */
public interface BillVisitor {
    // Visit regular bill
    void visit(Bill bill);

    // Default implementation for visiting any BillInterface
    // This allows visiting decorators without breaking existing visitors
    default void visit(BillInterface bill) {
        // By default, treat any BillInterface as a regular Bill
        if (bill instanceof Bill) {
            visit((Bill) bill);
        } else {
            // For decorators, we can access their specific features
            // but still process them as bills
            visitGeneric(bill);
        }
    }

    // Generic visit method for any bill type
    default void visitGeneric(BillInterface bill) {
        // Default implementation that works with any bill
        // Subclasses can override for specific behavior
    }
}