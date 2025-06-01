package com.syos.domain.valueobjects;

import java.util.Objects;

public final class BillNumber {
    private final int number;

    public BillNumber(int number) {
        if (number <= 0) {
            throw new IllegalArgumentException("Bill number must be positive");
        }
        this.number = number;
    }

    public int getValue() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillNumber that = (BillNumber) o;
        return number == that.number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return String.format("BILL-%06d", number);
    }
}