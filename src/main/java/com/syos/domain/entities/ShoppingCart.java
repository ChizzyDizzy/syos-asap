package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;
import java.time.LocalDateTime;
import java.util.*;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shopping Cart Entity
 * Thread-safe implementation for concurrent access
 */
public class ShoppingCart {
    private final CartId id;
    private final UserId userId;
    private final String sessionId;
    private final Map<String, CartItem> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CartStatus status;
    private final Object lock = new Object(); // For synchronization

    public ShoppingCart(UserId userId, String sessionId) {
        this.id = new CartId(generateCartId());
        this.userId = userId;
        this.sessionId = sessionId;
        this.items = new ConcurrentHashMap<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = CartStatus.ACTIVE;
    }

    /**
     * Add item to cart with thread safety
     * Demonstrates concurrent access control
     */
    public void addItem(Item item, int quantity) {
        synchronized (lock) {
            validateCartActive();
            
            String itemCode = item.getCode().getValue();
            if (items.containsKey(itemCode)) {
                // Update existing item quantity
                CartItem existing = items.get(itemCode);
                existing.updateQuantity(existing.getQuantity() + quantity);
            } else {
                // Add new item
                items.put(itemCode, new CartItem(item, quantity));
            }
            
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Remove item from cart
     */
    public void removeItem(String itemCode) {
        synchronized (lock) {
            validateCartActive();
            
            if (items.remove(itemCode) != null) {
                this.updatedAt = LocalDateTime.now();
            }
        }
    }

    /**
     * Update item quantity
     */
    public void updateItemQuantity(String itemCode, int newQuantity) {
        synchronized (lock) {
            validateCartActive();
            
            if (newQuantity <= 0) {
                removeItem(itemCode);
            } else if (items.containsKey(itemCode)) {
                items.get(itemCode).updateQuantity(newQuantity);
                this.updatedAt = LocalDateTime.now();
            }
        }
    }

    /**
     * Calculate total cart value
     */
    public Money calculateTotal() {
        return items.values().stream()
            .map(CartItem::getTotalPrice)
            .reduce(new Money(BigDecimal.ZERO), Money::add);
    }

    /**
     * Convert cart to order
     * Template Method Pattern
     */
    public OnlineOrder convertToOrder(OnlineCustomer customer, PaymentMethod paymentMethod) {
        synchronized (lock) {
            validateCartActive();
            
            // Create bill items from cart items
            List<BillItem> billItems = new ArrayList<>();
            for (CartItem cartItem : items.values()) {
                billItems.add(new BillItem(cartItem.getItem(), cartItem.getQuantity()));
            }
            
            // Create the order
            OnlineOrder order = new OnlineOrder.Builder()
                .withCustomer(customer)
                .withItems(billItems)
                .withPaymentMethod(paymentMethod)
                .withDeliveryAddress(customer.getDeliveryAddress())
                .build();
            
            // Mark cart as checked out
            this.status = CartStatus.CHECKED_OUT;
            this.updatedAt = LocalDateTime.now();
            
            return order;
        }
    }

    /**
     * Clear all items from cart
     */
    public void clear() {
        synchronized (lock) {
            validateCartActive();
            items.clear();
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Check if cart is expired (over 24 hours old)
     */
    public boolean isExpired() {
        return createdAt.plusHours(24).isBefore(LocalDateTime.now());
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    private void validateCartActive() {
        if (status != CartStatus.ACTIVE) {
            throw new IllegalStateException("Cart is not active: " + status);
        }
        if (isExpired()) {
            status = CartStatus.ABANDONED;
            throw new IllegalStateException("Cart has expired");
        }
    }

    private Long generateCartId() {
        return System.currentTimeMillis();
    }

    // Getters
    public CartId getId() { return id; }
    public UserId getUserId() { return userId; }
    public String getSessionId() { return sessionId; }
    public Map<String, CartItem> getItems() { 
        return Collections.unmodifiableMap(items); 
    }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public CartStatus getStatus() { return status; }
    public int getItemCount() { return items.size(); }
}
