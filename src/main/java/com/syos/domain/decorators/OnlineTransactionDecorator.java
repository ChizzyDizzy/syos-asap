package com.syos.domain.decorators;

import com.syos.domain.entities.*;
import com.syos.domain.interfaces.BillInterface;
import com.syos.domain.interfaces.BillVisitor;
import com.syos.domain.valueobjects.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Decorator pattern implementation for online transactions
 * Adds online-specific functionality to a Bill
 */

// single responsibility
public class OnlineTransactionDecorator implements BillInterface {
    private final Bill bill;
    private final String customerEmail;
    private final String deliveryAddress;
    private String trackingNumber;
    private LocalDateTime estimatedDeliveryDate;

    public OnlineTransactionDecorator(Bill bill, String customerEmail, String deliveryAddress) {
        validateInputs(bill, customerEmail, deliveryAddress);
        this.bill = bill;
        this.customerEmail = customerEmail;
        this.deliveryAddress = deliveryAddress;
        this.trackingNumber = generateTrackingNumber();
        this.estimatedDeliveryDate = calculateEstimatedDelivery();
    }

    private void validateInputs(Bill bill, String email, String address) {
        if (bill == null) {
            throw new IllegalArgumentException("Bill cannot be null");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer email is required");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery address is required");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    // Delegate all BillInterface methods to the wrapped bill
    @Override
    public BillNumber getBillNumber() {
        return bill.getBillNumber();
    }

    @Override
    public LocalDateTime getBillDate() {
        return bill.getBillDate();
    }

    @Override
    public List<BillItem> getItems() {
        return bill.getItems();
    }

    @Override
    public Money getTotalAmount() {
        return bill.getTotalAmount();
    }

    @Override
    public Money getDiscount() {
        return bill.getDiscount();
    }

    @Override
    public Money getCashTendered() {
        return bill.getCashTendered();
    }

    @Override
    public Money getChange() {
        return bill.getChange();
    }

    @Override
    public TransactionType getTransactionType() {
        // Always return ONLINE for decorated bills
        return TransactionType.ONLINE;
    }

    @Override
    public Money getFinalAmount() {
        return bill.getFinalAmount();
    }

    @Override
    public void accept(BillVisitor visitor) {
        // Visit this decorator
        visitor.visit(this.getOriginalBill());
        // Also visit the wrapped bill if needed
        bill.accept(visitor);
    }

    // Additional online-specific functionality
    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public LocalDateTime getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void sendEmailConfirmation() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📧 EMAIL CONFIRMATION");
        System.out.println("=".repeat(60));
        System.out.println("To: " + customerEmail);
        System.out.println("Subject: Order Confirmation - " + getBillNumber());
        System.out.println("\nDear Customer,");
        System.out.println("\nYour order has been successfully placed!");
        System.out.println("\nOrder Details:");
        System.out.println("- Order Number: " + getBillNumber());
        System.out.println("- Order Date: " + getBillDate());
        System.out.println("- Total Amount: " + getFinalAmount());
        System.out.println("- Delivery Address: " + deliveryAddress);
        System.out.println("- Tracking Number: " + trackingNumber);
        System.out.println("- Estimated Delivery: " + estimatedDeliveryDate);
        System.out.println("\nThank you for shopping with SYOS!");
        System.out.println("=".repeat(60) + "\n");
    }

    public void scheduleDelivery() {
        System.out.println("✓ Delivery scheduled");
        System.out.println("  Address: " + deliveryAddress);
        System.out.println("  Estimated delivery: " + estimatedDeliveryDate);
        System.out.println("  Tracking number: " + trackingNumber);
    }

    public void processOnlinePayment() {
        System.out.println("💳 Processing online payment...");
        System.out.println("  Amount: " + getFinalAmount());
        System.out.println("  Status: Payment successful");
        System.out.println("  Confirmation sent to: " + customerEmail);
    }

    public void updateDeliveryStatus(String status) {
        System.out.println("📦 Delivery status updated: " + status);
        // In real implementation, this would update database and send notification
    }

    private String generateTrackingNumber() {
        return "SYOS-" + System.currentTimeMillis() + "-" + getBillNumber().getValue();
    }

    private LocalDateTime calculateEstimatedDelivery() {
        // Add 3-5 business days for delivery
        return getBillDate().plusDays(3);
    }

    // Get the original bill (if needed)
    public Bill getOriginalBill() {
        return bill;
    }
}