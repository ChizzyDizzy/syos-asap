package com.syos.domain.entities;

import com.syos.domain.exceptions.InvalidStateTransitionException;
import com.syos.domain.interfaces.ItemState;

public class SoldOutState implements ItemState {
    @Override
    public void moveToShelf(Item item, int amount) {
        throw new InvalidStateTransitionException("No items available to move");
    }

    @Override
    public void sell(Item item, int amount) {
        throw new InvalidStateTransitionException("Item is sold out");
    }

    @Override
    public void expire(Item item) {
        // Sold out items don't expire
    }

    @Override
    public String getStateName() {
        return "SOLD_OUT";
    }
}