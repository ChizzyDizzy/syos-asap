package com.syos.domain.entities;

import com.syos.domain.exceptions.InvalidStateTransitionException;

public class InStoreState implements ItemState {
    @Override
    public void moveToShelf(Item item, int amount) {
        if (item.getQuantity().getValue() < amount) {
            throw new InvalidStateTransitionException("Not enough items in store");
        }
        item.reduceQuantity(amount);
        // In real implementation, would create shelf item
    }

    @Override
    public void sell(Item item, int amount) {
        throw new InvalidStateTransitionException("Cannot sell items directly from store");
    }

    @Override
    public void expire(Item item) {
        item.setState(new ExpiredState());
    }

    @Override
    public String getStateName() {
        return "IN_STORE";
    }
}