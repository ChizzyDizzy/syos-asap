package com.syos.web.model;

public class SaleItem {
    private long id;
    private long saleId;
    private String itemCode;
    private String itemName;
    private int quantity;
    private double unitPrice;
    private double subtotal;

    public SaleItem() {}

    public SaleItem(long saleId, String itemCode, String itemName, int quantity, double unitPrice) {
        this.saleId = saleId;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = quantity * unitPrice;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSaleId() {
        return saleId;
    }

    public void setSaleId(long saleId) {
        this.saleId = saleId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.subtotal = quantity * unitPrice;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.subtotal = quantity * unitPrice;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return String.format("SaleItem[saleId=%d, item=%s, qty=%d, price=%.2f, subtotal=%.2f]",
                saleId, itemCode, quantity, unitPrice, subtotal);
    }
}