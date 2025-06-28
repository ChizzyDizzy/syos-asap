package com.syos.application.services;

import com.syos.domain.decorators.OnlineTransactionDecorator;
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
import java.util.stream.Collectors;

/**
 * Service class for handling sales operations
 * Manages the creation and processing of sales transactions
 */
public class SalesService {
    private final BillGateway billGateway;
    private final ItemGateway itemGateway;

    public SalesService(BillGateway billGateway, ItemGateway itemGateway) {
        this.billGateway = billGateway;
        this.itemGateway = itemGateway;
    }

    /**
     * Start a new sale transaction
     * @return A new SaleBuilder instance
     */
    public SaleBuilder startNewSale() {
        return new SaleBuilder();
    }

    /**
     * Save a completed bill to the database and update inventory
     * @param bill The bill to save
     */
    public void saveBill(Bill bill) {
        billGateway.saveBillWithItems(bill);

        // Update item quantities
        for (BillItem billItem : bill.getItems()) {
            Item item = billItem.getItem();
            item.sell(billItem.getQuantity().getValue());
            itemGateway.update(item);
        }
    }

    /**
     * Get all bills for today's date
     * @return List of bills from today
     */
    public List<Bill> getBillsForToday() {
        return billGateway.findByDate(LocalDate.now());
    }

    /**
     * Get all bills from the database
     * @return List of all bills
     */
    public List<Bill> getAllBills() {
        return billGateway.findAll();
    }

    /**
     * Get bills for a specific date
     * @param date The date to search for
     * @return List of bills from the specified date
     */
    public List<Bill> getBillsByDate(LocalDate date) {
        return billGateway.findByDate(date);
    }

    /**
     * Get a specific bill by its number
     * @param billNumber The bill number to search for
     * @return The bill if found, null otherwise
     */
    public Bill getBillByNumber(int billNumber) {
        return billGateway.findByBillNumber(billNumber);
    }

    /**
     * Check if an item is available for sale
     * @param itemCode The item code to check
     * @return true if item is on shelf with stock > 0
     */
    public boolean isItemAvailable(String itemCode) {
        Item item = itemGateway.findByCode(itemCode);
        return item != null &&
                "ON_SHELF".equals(item.getState().getStateName()) &&
                item.getQuantity().getValue() > 0;
    }

    /**
     * Get all items available for sale (on shelf with stock)
     * @return List of available items
     */
    public List<Item> getAvailableItems() {
        return itemGateway.findAll().stream()
                .filter(item -> "ON_SHELF".equals(item.getState().getStateName()))
                .filter(item -> item.getQuantity().getValue() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Inner class for building sales transactions
     * Implements the Builder pattern for creating bills
     */
    public class SaleBuilder {
        private final List<BillItem> items = new ArrayList<>();
        private Money subtotal = new Money(BigDecimal.ZERO);

        /**
         * Add an item to the current sale
         * @param itemCode The code of the item to add
         * @param quantity The quantity to add
         * @return This builder instance for method chaining
         * @throws ItemNotFoundException if item doesn't exist
         * @throws InsufficientStockException if not enough stock
         */
        public SaleBuilder addItem(String itemCode, int quantity) {
            Item item = itemGateway.findByCode(itemCode);
            if (item == null) {
                throw new ItemNotFoundException("Item with code " + itemCode + " not found");
            }

            if (!"ON_SHELF".equals(item.getState().getStateName())) {
                throw new IllegalStateException("Item " + itemCode + " is not available for sale. Current state: " + item.getState().getStateName());
            }

            if (item.getQuantity().getValue() < quantity) {
                throw new InsufficientStockException("Not enough stock for item " + item.getName() +
                        ". Available: " + item.getQuantity().getValue() + ", Requested: " + quantity);
            }

            BillItem billItem = new BillItem(item, quantity);
            items.add(billItem);
            subtotal = subtotal.add(billItem.getTotalPrice());

            return this;
        }

        /**
         * Remove an item from the current sale
         * @param itemCode The code of the item to remove
         * @return This builder instance for method chaining
         */
        public SaleBuilder removeItem(String itemCode) {
            items.removeIf(billItem -> billItem.getItem().getCode().getValue().equals(itemCode));
            recalculateSubtotal();
            return this;
        }

        /**
         * Get the current subtotal of the sale
         * @return The subtotal amount
         */
        public Money getSubtotal() {
            return subtotal;
        }

        /**
         * Get the list of items in the current sale
         * @return List of bill items
         */
        public List<BillItem> getItems() {
            return new ArrayList<>(items);
        }

        /**
         * Complete the sale and create a bill
         * @param cashTendered The amount of cash provided by customer
         * @return The completed bill
         * @throws EmptySaleException if no items in the sale
         * @throws IllegalArgumentException if cash tendered is insufficient
         */
        public Bill completeSale(BigDecimal cashTendered) {
            if (items.isEmpty()) {
                throw new EmptySaleException("Cannot complete sale with no items");
            }

            if (cashTendered.compareTo(subtotal.getValue()) < 0) {
                throw new IllegalArgumentException("Insufficient cash. Required: " + subtotal + ", Tendered: " + cashTendered);
            }

            // Create the bill with all items
            Bill.Builder billBuilder = new Bill.Builder()
                    .withBillNumber(generateBillNumber())
                    .withDate(LocalDateTime.now())
                    .withDiscount(BigDecimal.ZERO)
                    .withCashTendered(cashTendered)
                    .withTransactionType(TransactionType.IN_STORE);

            // Add all items to the bill
            for (BillItem item : items) {
                billBuilder.addBillItem(item);
            }

            return billBuilder.build();
        }

        /**
         * Complete an online sale
         * @param cashTendered The amount paid
         * @param customerEmail The customer's email
         * @param deliveryAddress The delivery address
         * @return The completed bill with online transaction decorator
         */
        public Bill completeOnlineSale(BigDecimal cashTendered, String customerEmail, String deliveryAddress) {
            Bill baseBill = completeSale(cashTendered);
            return new OnlineTransactionDecorator(baseBill, customerEmail, deliveryAddress).getOriginalBill();
        }

        /**
         * Clear all items from the current sale
         */
        public void clearSale() {
            items.clear();
            subtotal = new Money(BigDecimal.ZERO);
        }

        /**
         * Recalculate the subtotal after modifications
         */
        private void recalculateSubtotal() {
            subtotal = items.stream()
                    .map(BillItem::getTotalPrice)
                    .reduce(new Money(BigDecimal.ZERO), Money::add);
        }

        /**
         * Generate a unique bill number
         * @return A unique bill number
         */
        private int generateBillNumber() {
            // In a production system, this would:
            // 1. Query the database for the last bill number
            // 2. Increment and return the next number
            // 3. Or use a sequence/auto-increment in the database

            // For now, use timestamp-based generation
            return (int) (System.currentTimeMillis() % 1000000);
        }
    }
}