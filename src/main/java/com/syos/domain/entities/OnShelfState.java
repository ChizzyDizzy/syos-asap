package com.syos.domain.entities;

import com.syos.domain.exceptions.InvalidStateTransitionException;

public class OnShelfState implements ItemState {
    @Override
    public void moveToShelf(Item item, int amount) {
        throw new InvalidStateTransitionException("Item is already on shelf");
    }

    @Override
    public void sell(Item item, int amount) {
        if (item.getQuantity().getValue() < amount) {
            throw new InvalidStateTransitionException("Not enough items on shelf");
        }
        item.reduceQuantity(amount);
        if (item.getQuantity().getValue() == 0) {
            item.setState(new SoldOutState());
        }
    }

    @Override
    public void expire(Item item) {
        item.setState(new ExpiredState());
    }

    @Override
    public String getStateName() {
        return "ON_SHELF";
    }
}