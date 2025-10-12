package com.syos.domain.entities;

import com.syos.domain.interfaces.BillInterface;
import com.syos.domain.interfaces.BillVisitor;
import com.syos.domain.valueobjects.*;
import com.syos.domain.exceptions.*;
import java.time.LocalDateTime;
import java.util.*;
import java.math.BigDecimal;

/**
 * Bill Domain Entity
 * Represents a sales transaction in the system
 */
public class Bill implements BillInterface {
    private final BillNumber billNumber;
    private final LocalDateTime billDate;
    private final List<BillItem> items;
    private final Money totalAmount;
    private final Money discount;
    private final Money cashTendered;
    private final Money change;
    private final TransactionType transactionType;

    // Private constructor - use Builder
    private Bill(BillNumber billNumber, LocalDateTime billDate, List<BillItem> items,
                 Money totalAmount, Money discount, Money cashTendered, Money change,
                 TransactionType transactionType) {
        this.billNumber = billNumber;
        this.billDate = billDate;
        this.items = Collections.unmodifiableList(items);
        this.totalAmount = totalAmount;
        this.discount = discount;
        this.cashTendered = cashTendered;
        this.change = change;
        this.transactionType = transactionType;
    }

    // Builder Pattern
    public static class Builder {
        private BillNumber billNumber;
        private LocalDateTime billDate = LocalDateTime.now();
        private List<BillItem> items = new ArrayList<>();
        private Money discount = new Money(BigDecimal.ZERO);
        private Money cashTendered;
        private TransactionType transactionType = TransactionType.IN_STORE;

        public Builder withBillNumber(int number) {
            this.billNumber = new BillNumber(number);
            return this;
        }

        public Builder withDate(LocalDateTime date) {
            this.billDate = date;
            return this;
        }

        public Builder addItem(Item item, int quantity) {
            items.add(new BillItem(item, quantity));
            return this;
        }

        public Builder addBillItem(BillItem billItem) {
            items.add(billItem);
            return this;
        }

        public Builder withItems(List<BillItem> items) {
            this.items = new ArrayList<>(items);
            return this;
        }

        public Builder withDiscount(BigDecimal discount) {
            this.discount = new Money(discount);
            return this;
        }

        public Builder withCashTendered(BigDecimal cash) {
            this.cashTendered = new Money(cash);
            return this;
        }

        public Builder withTransactionType(TransactionType type) {
            this.transactionType = type;
            return this;
        }

        public Bill build() {
            validateBill();

            Money total = calculateTotal();
            Money finalAmount = total.subtract(discount);
            Money change = cashTendered.subtract(finalAmount);

            if (change.isNegative()) {
                throw new InsufficientPaymentException(
                        "Cash tendered (" + cashTendered + ") is insufficient for amount " + finalAmount);
            }

            return new Bill(billNumber, billDate, items, total, discount,
                    cashTendered, change, transactionType);
        }

        private void validateBill() {
            if (billNumber == null) {
                throw new IllegalStateException("Bill number is required");
            }
            if (items.isEmpty()) {
                throw new EmptySaleException("Cannot create bill with no items");
            }
            if (cashTendered == null) {
                throw new IllegalStateException("Cash tendered amount is required");
            }
        }


        private Money calculateTotal() {
            return items.stream()
                    .map(BillItem::getTotalPrice)
                    .reduce(new Money(BigDecimal.ZERO), Money::add);
        }
    }

    // Getters
    @Override
    public BillNumber getBillNumber() {
        return billNumber;
    }

    @Override
    public LocalDateTime getBillDate() {
        return billDate;
    }

    @Override
    public List<BillItem> getItems() {
        return items;
    }

    @Override
    public Money getTotalAmount() {
        return totalAmount;
    }

    @Override
    public Money getDiscount() {
        return discount;
    }

    @Override
    public Money getCashTendered() {
        return cashTendered;
    }

    @Override
    public Money getChange() {
        return change;
    }

    @Override
    public TransactionType getTransactionType() {
        return transactionType;
    }

    @Override
    public Money getFinalAmount() {
        return totalAmount.subtract(discount);
    }

    // Visitor Pattern support
    @Override
    public void accept(BillVisitor visitor) {
        visitor.visit(this);
    }

    // Utility methods
    public int getTotalItemCount() {
        return items.stream()
                .mapToInt(item -> item.getQuantity().getValue())
                .sum();
    }

    public boolean hasDiscount() {
        return discount.getValue().compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public String toString() {
        return String.format("Bill[number=%s, date=%s, items=%d, total=%s]",
                billNumber, billDate, items.size(), getFinalAmount());
    }
}