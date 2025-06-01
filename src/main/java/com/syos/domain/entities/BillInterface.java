package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for Bill entities to support Decorator pattern
 */
public interface BillInterface {
    // Core bill attributes
    BillNumber getBillNumber();
    LocalDateTime getBillDate();
    List<BillItem> getItems();
    Money getTotalAmount();
    Money getDiscount();
    Money getCashTendered();
    Money getChange();
    TransactionType getTransactionType();
    Money getFinalAmount();

    // Visitor pattern support
    void accept(BillVisitor visitor);
}