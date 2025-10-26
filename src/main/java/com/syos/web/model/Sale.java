package com.syos.web.model;

import java.sql.Timestamp;

public class Sale {
    private long id;
    private String saleNumber;
    private long cashierId;
    private double totalAmount;
    private double discount;
    private double taxAmount;
    private String paymentMethod;
    private double cashTendered;
    private double changeAmount;
    private String status;
    private Timestamp createdAt;
    private int version;

    public Sale() {}

    public Sale(String saleNumber, long cashierId, double totalAmount) {
        this.saleNumber = saleNumber;
        this.cashierId = cashierId;
        this.totalAmount = totalAmount;
        this.version = 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSaleNumber() {
        return saleNumber;
    }

    public void setSaleNumber(String saleNumber) {
        this.saleNumber = saleNumber;
    }

    public long getCashierId() {
        return cashierId;
    }

    public void setCashierId(long cashierId) {
        this.cashierId = cashierId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getCashTendered() {
        return cashTendered;
    }

    public void setCashTendered(double cashTendered) {
        this.cashTendered = cashTendered;
    }

    public double getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(double changeAmount) {
        this.changeAmount = changeAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return String.format("Sale[id=%d, number=%s, cashier=%d, total=%.2f, status=%s]",
                id, saleNumber, cashierId, totalAmount, status);
    }
}