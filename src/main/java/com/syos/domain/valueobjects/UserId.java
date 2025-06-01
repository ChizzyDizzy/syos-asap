package com.syos.domain.valueobjects;

import java.util.Objects;

public final class UserId {
    private final Long id;

    public UserId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        this.id = id;
    }

    public Long getValue() { return id; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(id, userId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}