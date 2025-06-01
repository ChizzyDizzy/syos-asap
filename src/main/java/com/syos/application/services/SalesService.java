package com.syos.application.services;

import com.syos.domain.entities.*;
import com.syos.domain.exceptions.EmptySaleException;
import com.syos.domain.exceptions.InsufficientStockException;
import com.syos.domain.exceptions.ItemNotFoundException;
import com.syos.domain.valueobjects.*;
import com.syos.infrastructure.persistence.gateways.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalesService {
    private final BillGateway billGateway;
    private final ItemGateway itemGateway;

    public SalesService(BillGateway billGateway, ItemGateway itemGateway) {
        this.billGateway = billGateway;
        this.itemGateway = itemGateway;
    }

    public SaleBuilder startNewSale() {
        return new SaleBuilder();
    }

    public void saveBill(Bill bill) {
        billGateway.saveBillWithItems(bill);

        // Update item quantities
        for (BillItem billItem : bill.getItems()) {
            Item item = billItem.getItem();
            item.sell(billItem.getQuantity().getValue());
            itemGateway.update(item);
        }
    }

    public List<Bill> getBillsForToday() {
        return billGateway.findByDate(LocalDate.now());
    }

    // Inner class for building sales
    public class SaleBuilder {
        private final List<BillItem> items = new ArrayList<>();
        private Money subtotal = new Money(BigDecimal.ZERO);

        public SaleBuilder addItem(String itemCode, int quantity) {
            Item item = itemGateway.findByCode(itemCode);
            if (item == null) {
                throw new ItemNotFoundException("Item with code " + itemCode + " not found");
            }

            if (item.getQuantity().getValue() < quantity) {
                throw new InsufficientStockException("Not enough stock for item " + item.getName());
            }

            BillItem billItem = new BillItem(item, quantity);
            items.add(billItem);
            subtotal = subtotal.add(billItem.getTotalPrice());

            return this;
        }

        public Money getSubtotal() {
            return subtotal;
        }

        public Bill completeSale(BigDecimal cashTendered) {
            if (items.isEmpty()) {
                throw new EmptySaleException("Cannot complete sale with no items");
            }

            return new Bill.Builder()
                    .withBillNumber(generateBillNumber())
                    .withDate(LocalDateTime.now())
                    .withDiscount(BigDecimal.ZERO) // Can be enhanced later
                    .withCashTendered(cashTendered)
                    .withTransactionType(TransactionType.IN_STORE)
                    .build();
        }

        private int generateBillNumber() {
            // In production, this would query the database for the next number
            return (int) (System.currentTimeMillis() % 1000000);
        }
    }
}