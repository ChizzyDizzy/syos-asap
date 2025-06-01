package com.syos.domain.entities;

public interface ItemState {
    void moveToShelf(Item item, int amount);
    void sell(Item item, int amount);
    void expire(Item item);
    String getStateName();
}
