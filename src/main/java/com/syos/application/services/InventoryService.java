package com.syos.application.services;

import com.syos.domain.entities.*;
import com.syos.domain.valueobjects.*;
import com.syos.domain.exceptions.*;
import com.syos.infrastructure.persistence.gateways.ItemGateway;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Service for Inventory Management
 * Orchestrates inventory-related use cases
 */
public class InventoryService {
    private final ItemGateway itemGateway;
    private static final int REORDER_THRESHOLD = 50;
    private static final int EXPIRY_WARNING_DAYS = 7;

    public InventoryService(ItemGateway itemGateway) {
        this.itemGateway = itemGateway;
    }

    /**
     * Add new stock to inventory
     */
    public void addStock(String code, String name, BigDecimal price,
                         int quantity, LocalDate expiryDate) {
        // Check if item already exists
        Item existingItem = itemGateway.findByCode(code);

        if (existingItem != null) {
            // Update existing item quantity
            int newQuantity = existingItem.getQuantity().getValue() + quantity;
            Item updatedItem = new Item.Builder()
                    .withCode(code)
                    .withName(existingItem.getName())
                    .withPrice(existingItem.getPrice().getValue())
                    .withQuantity(newQuantity)
                    .withExpiryDate(existingItem.getExpiryDate())
                    .withState(existingItem.getState())
                    .withPurchaseDate(existingItem.getPurchaseDate())
                    .build();

            itemGateway.update(updatedItem);
        } else {
            // Create new item
            Item newItem = new Item.Builder()
                    .withCode(code)
                    .withName(name)
                    .withPrice(price)
                    .withQuantity(quantity)
                    .withExpiryDate(expiryDate)
                    .withState(new InStoreState())
                    .withPurchaseDate(LocalDate.now())
                    .build();

            itemGateway.insert(newItem);
        }
    }

    /**
     * Move items from store to shelf
     */
    public void moveToShelf(String itemCode, int quantity) {
        Item item = itemGateway.findByCode(itemCode);
        if (item == null) {
            throw new ItemNotFoundException("Item not found: " + itemCode);
        }

        // Use state pattern to move to shelf
        item.moveToShelf(quantity);

        // Create shelf item (in real implementation, might be separate entity)
        Item shelfItem = itemGateway.findByCode(itemCode + "_SHELF");
        if (shelfItem != null) {
            // Update existing shelf item
            int newQuantity = shelfItem.getQuantity().getValue() + quantity;
            Item updatedShelfItem = new Item.Builder()
                    .withCode(shelfItem.getCode().getValue())
                    .withName(shelfItem.getName())
                    .withPrice(shelfItem.getPrice().getValue())
                    .withQuantity(newQuantity)
                    .withExpiryDate(item.getExpiryDate())
                    .withState(new OnShelfState())
                    .withPurchaseDate(shelfItem.getPurchaseDate())
                    .build();
            itemGateway.update(updatedShelfItem);
        } else {
            // Create new shelf item
            Item newShelfItem = new Item.Builder()
                    .withCode(itemCode + "_SHELF")
                    .withName(item.getName())
                    .withPrice(item.getPrice().getValue())
                    .withQuantity(quantity)
                    .withExpiryDate(item.getExpiryDate())
                    .withState(new OnShelfState())
                    .withPurchaseDate(LocalDate.now())
                    .build();
            itemGateway.insert(newShelfItem);
        }

        // Update store item
        itemGateway.update(item);
    }

    /**
     * Get all items
     */
    public List<Item> getAllItems() {
        return itemGateway.findAll();
    }

    /**
     * Get items currently in store (not on shelf)
     */
    public List<Item> getItemsInStore() {
        return itemGateway.findAll().stream()
                .filter(item -> "IN_STORE".equals(item.getState().getStateName()))
                .collect(Collectors.toList());
    }

    /**
     * Get items currently on shelf
     */
    public List<Item> getItemsOnShelf() {
        return itemGateway.findAll().stream()
                .filter(item -> "ON_SHELF".equals(item.getState().getStateName()))
                .collect(Collectors.toList());
    }

    /**
     * Get item by code
     */
    public Item getItemByCode(String code) {
        return itemGateway.findByCode(code);
    }

    /**
     * Get low stock items
     */
    public List<Item> getLowStockItems() {
        return itemGateway.findLowStock(REORDER_THRESHOLD);
    }

    /**
     * Get items expiring soon
     */
    public List<Item> getExpiringItems(int daysAhead) {
        return itemGateway.findExpiringSoon(daysAhead);
    }

    /**
     * Get items expiring within default warning period
     */
    public List<Item> getExpiringItems() {
        return getExpiringItems(EXPIRY_WARNING_DAYS);
    }

    /**
     * Check and update expired items
     */
    public void checkAndUpdateExpiredItems() {
        List<Item> items = itemGateway.findAll();
        for (Item item : items) {
            if (item.isExpired() && !item.getState().getStateName().equals("EXPIRED")) {
                item.expire();
                itemGateway.update(item);
            }
        }
    }

    /**
     * Update item price
     */
    public void updateItemPrice(String itemCode, BigDecimal newPrice) {
        Item item = itemGateway.findByCode(itemCode);
        if (item == null) {
            throw new ItemNotFoundException("Item not found: " + itemCode);
        }

        Item updatedItem = new Item.Builder()
                .withCode(item.getCode().getValue())
                .withName(item.getName())
                .withPrice(newPrice)
                .withQuantity(item.getQuantity().getValue())
                .withExpiryDate(item.getExpiryDate())
                .withState(item.getState())
                .withPurchaseDate(item.getPurchaseDate())
                .build();

        itemGateway.update(updatedItem);
    }

    /**
     * Get total inventory value
     */
    public BigDecimal getTotalInventoryValue() {
        return itemGateway.findAll().stream()
                .filter(item -> !item.getState().getStateName().equals("EXPIRED"))
                .map(item -> item.getPrice().getValue()
                        .multiply(BigDecimal.valueOf(item.getQuantity().getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get inventory statistics
     */
    public InventoryStatistics getInventoryStatistics() {
        List<Item> allItems = itemGateway.findAll();

        int totalItems = allItems.size();
        int totalQuantity = allItems.stream()
                .mapToInt(item -> item.getQuantity().getValue())
                .sum();
        int expiredCount = (int) allItems.stream()
                .filter(item -> item.getState().getStateName().equals("EXPIRED"))
                .count();
        int lowStockCount = getLowStockItems().size();
        int expiringCount = getExpiringItems().size();
        BigDecimal totalValue = getTotalInventoryValue();

        return new InventoryStatistics(
                totalItems, totalQuantity, expiredCount,
                lowStockCount, expiringCount, totalValue
        );
    }

    /**
     * Inner class for inventory statistics
     */
    public static class InventoryStatistics {
        public final int totalItems;
        public final int totalQuantity;
        public final int expiredCount;
        public final int lowStockCount;
        public final int expiringCount;
        public final BigDecimal totalValue;

        public InventoryStatistics(int totalItems, int totalQuantity,
                                   int expiredCount, int lowStockCount,
                                   int expiringCount, BigDecimal totalValue) {
            this.totalItems = totalItems;
            this.totalQuantity = totalQuantity;
            this.expiredCount = expiredCount;
            this.lowStockCount = lowStockCount;
            this.expiringCount = expiringCount;
            this.totalValue = totalValue;
        }
    }
}