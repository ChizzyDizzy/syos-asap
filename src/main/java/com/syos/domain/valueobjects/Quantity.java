package com.syos.domain.valueobjects;

import com.syos.domain.exceptions.InvalidQuantityException;
import java.util.Objects;

public final class Quantity {
    private final int value;

    public Quantity(int value) {
        if (value < 0) {
            throw new InvalidQuantityException("Quantity cannot be negative");
        }
        this.value = value;
    }

    public Quantity add(int amount) {
        return new Quantity(this.value + amount);
    }

    public Quantity subtract(int amount) {
        if (this.value < amount) {
            throw new InvalidQuantityException("Cannot subtract more than available quantity");
        }
        return new Quantity(this.value - amount);
    }

    public boolean isGreaterThan(int amount) {
        return this.value > amount;
    }

    public boolean isLessThan(int amount) {
        return this.value < amount;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quantity quantity = (Quantity) o;
        return value == quantity.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
