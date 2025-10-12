package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;

public class BillItem {
    private final Item item;
    private final Quantity quantity;
    private final Money totalPrice;

    public BillItem(Item item, int quantity) {
        this.item = item;
        this.quantity = new Quantity(quantity);
        this.totalPrice = item.getPrice().multiply(quantity);
    }

    public Item getItem() { return item; }
    public Quantity getQuantity() { return quantity; }
    public Money getTotalPrice() { return totalPrice; }
}