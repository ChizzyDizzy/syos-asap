package com.syos.domain.valueobjects;

import com.syos.domain.exceptions.InvalidItemCodeException;
import java.util.Objects;

public final class ItemCode {
    private final String code;

    public ItemCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidItemCodeException("Item code cannot be null or empty");
        }
        if (!code.matches("^[A-Z0-9]{4,10}$")) {
            throw new InvalidItemCodeException("Item code must be 4-10 alphanumeric characters");
        }
        this.code = code.toUpperCase();
    }

    public String getValue() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemCode itemCode = (ItemCode) o;
        return Objects.equals(code, itemCode.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}