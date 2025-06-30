package com.syos.domain.entities;

import com.syos.domain.exceptions.InvalidStateTransitionException;
import com.syos.domain.interfaces.ItemState;

public class ExpiredState implements ItemState {
    @Override
    public void moveToShelf(Item item, int amount) {
        throw new InvalidStateTransitionException("Cannot move expired items to shelf");
    }

    @Override
    public void sell(Item item, int amount) {
        throw new InvalidStateTransitionException("Cannot sell expired items");
    }

    @Override
    public void expire(Item item) {
        // Already expired, do nothing
    }

    @Override
    public String getStateName() {
        return "EXPIRED";
    }
}
//ocp
//sr
//elimcon
//typsa
//itemli
//newst