package com.syos.domain.interfaces;

import com.syos.domain.entities.Item;

public interface ItemState {
    void moveToShelf(Item item, int amount);
    void sell(Item item, int amount);
    void expire(Item item);
    String getStateName();
}
