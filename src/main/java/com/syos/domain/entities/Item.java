package com.syos.domain.entities;

import com.syos.domain.interfaces.ItemState;
import com.syos.domain.valueobjects.*;
import com.syos.domain.exceptions.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

//thread safe design - builder pattern
public class Item {
    private final ItemCode code;
    private final String name;
    private final Money price;
    private final LocalDate expiryDate;
    private ItemState state;
    private final LocalDate purchaseDate;
    private Quantity quantity;

    // Builder Pattern for complex object construction
    public static class Builder {
        private ItemCode code;
        private String name;
        private Money price;
        private LocalDate expiryDate;
        private ItemState state;
        private LocalDate purchaseDate;
        private Quantity quantity;

        public Builder withCode(String code) {
            this.code = new ItemCode(code);
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withPrice(BigDecimal price) {
            this.price = new Money(price);
            return this;
        }

        public Builder withExpiryDate(LocalDate expiryDate) {
            this.expiryDate = expiryDate;
            return this;
        }

        public Builder withState(ItemState state) {
            this.state = state;
            return this;
        }

        public Builder withPurchaseDate(LocalDate purchaseDate) {
            this.purchaseDate = purchaseDate;
            return this;
        }

        public Builder withQuantity(int quantity) {
            this.quantity = new Quantity(quantity);
            return this;
        }

        public Item build() {
            validateItem();
            return new Item(this);
        }
    //validation construction
        private void validateItem() {
            if (code == null || name == null || price == null) {
                throw new InvalidItemException("Item must have code, name, and price");
            }
            if (quantity == null) {
                quantity = new Quantity(0);
            }
            if (state == null) {
                state = new InStoreState();
            }
            if (purchaseDate == null) {
                purchaseDate = LocalDate.now();
            }
        }
    }

    private Item(Builder builder) {
        this.code = builder.code;
        this.name = builder.name;
        this.price = builder.price;
        this.expiryDate = builder.expiryDate;
        this.state = builder.state;
        this.purchaseDate = builder.purchaseDate;
        this.quantity = builder.quantity;
    }

    // State Pattern methods
    public void moveToShelf(int amount) {
        state.moveToShelf(this, amount);
    }

    public void sell(int amount) {
        state.sell(this, amount);
    }

    public void expire() {
        state.expire(this);
    }

    public boolean isExpired() {
        return expiryDate != null && LocalDate.now().isAfter(expiryDate);
    }

    public void setState(ItemState newState) {
        this.state = newState;
    }

    public void reduceQuantity(int amount) {
        this.quantity = quantity.subtract(amount);
    }

    public void addQuantity(int amount) {
        this.quantity = quantity.add(amount);
    }

    // Getters
    public ItemCode getCode() { return code; }
    public String getName() { return name; }
    public Money getPrice() { return price; }
    public Quantity getQuantity() { return quantity; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public ItemState getState() { return state; }



    // optional parameters
    public long daysUntilExpiry() {
        if (expiryDate == null) {
            return Long.MAX_VALUE; // No expiry
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    /**
     * Check if item is expiring soon (within specified days)
     * @param daysThreshold number of days to check
     * @return true if expiring within threshold
     */
    public boolean isExpiringSoon(int daysThreshold) {
        if (expiryDate == null) {
            return false;
        }
        return daysUntilExpiry() <= daysThreshold;
    }

    /**
     * Get expiry status as string
     * @return expiry status description
     */
    public String getExpiryStatus() {
        if (expiryDate == null) {
            return "No expiry";
        }
        long days = daysUntilExpiry();
        if (days < 0) {
            return "EXPIRED";
        } else if (days == 0) {
            return "Expires today";
        } else if (days == 1) {
            return "Expires tomorrow";
        } else if (days <= 7) {
            return "Expires in " + days + " days";
        } else {
            return "Expires on " + expiryDate;
        }
    }
}