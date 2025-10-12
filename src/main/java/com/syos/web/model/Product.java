package com.syos.web.model;

import java.sql.Date;

public class Product {
    private String itemCode;
    private String name;
    private String category;
    private double price;
    private int quantityInStore;
    private int quantityOnShelf;
    private int reorderLevel;
    private String state;
    private Date expiryDate;

    public Product() {}

    // Getters and Setters
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantityInStore() { return quantityInStore; }
    public void setQuantityInStore(int quantityInStore) { this.quantityInStore = quantityInStore; }

    public int getQuantityOnShelf() { return quantityOnShelf; }
    public void setQuantityOnShelf(int quantityOnShelf) { this.quantityOnShelf = quantityOnShelf; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public int getTotalQuantity() {
        return quantityInStore + quantityOnShelf;
    }
}