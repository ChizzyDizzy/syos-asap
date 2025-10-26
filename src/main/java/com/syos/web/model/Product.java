package com.syos.web.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Product {
    private String code;
    private String name;
    private String category;
    private double price;
    private int quantityInStore;
    private int quantityOnShelf;
    private int reorderLevel;
    private String state;
    private Date purchaseDate;
    private Date expiryDate;
    private int version;
    private Long lockedBy;
    private Timestamp lockTimestamp;

    public Product() {
        this.version = 0;
    }

    public Product(String code, String name, String category, double price,
                   int quantityInStore, int quantityOnShelf, int reorderLevel) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantityInStore = quantityInStore;
        this.quantityOnShelf = quantityOnShelf;
        this.reorderLevel = reorderLevel;
        this.state = "AVAILABLE";
        this.version = 0;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantityInStore() {
        return quantityInStore;
    }

    public void setQuantityInStore(int quantityInStore) {
        this.quantityInStore = quantityInStore;
    }

    public int getQuantityOnShelf() {
        return quantityOnShelf;
    }

    public void setQuantityOnShelf(int quantityOnShelf) {
        this.quantityOnShelf = quantityOnShelf;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Long getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(Long lockedBy) {
        this.lockedBy = lockedBy;
    }

    public Timestamp getLockTimestamp() {
        return lockTimestamp;
    }

    public void setLockTimestamp(Timestamp lockTimestamp) {
        this.lockTimestamp = lockTimestamp;
    }

    public int getTotalStock() {
        return quantityInStore + quantityOnShelf;
    }

    public boolean needsReorder() {
        return getTotalStock() <= reorderLevel;
    }

    @Override
    public String toString() {
        return String.format("Product[code=%s, name=%s, category=%s, price=%.2f, store=%d, shelf=%d, version=%d]",
                code, name, category, price, quantityInStore, quantityOnShelf, version);
    }
}