package com.syos.web.model;

import java.sql.Timestamp;

public class Bill {
    private long id;
    private String billNumber;
    private long userId;
    private double totalAmount;
    private double discount;
    private double taxAmount;
    private double netAmount;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int version;

    public Bill() {
        this.version = 0;
    }

    public Bill(String billNumber, long userId, double totalAmount) {
        this.billNumber = billNumber;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.status = "PENDING";
        this.version = 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
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

    public double getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(double netAmount) {
        this.netAmount = netAmount;
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

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return String.format("Bill[id=%d, number=%s, user=%d, total=%.2f, status=%s, version=%d]",
                id, billNumber, userId, totalAmount, status, version);
    }
}