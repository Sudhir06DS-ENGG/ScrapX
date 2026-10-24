package com.scrapx.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
    Buyer,
    Admin,
    Seller;

    @JsonCreator
    public static UserRole fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Buyer;
        }
        for (UserRole role : UserRole.values()) {
            if (role.name().equalsIgnoreCase(value.trim())) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role: '" + value + "'. Allowed roles are: Buyer, Seller, Admin");
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
