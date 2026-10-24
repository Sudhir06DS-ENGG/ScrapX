package com.scrapx.listing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ListingType {
    SINGLE,
    BULK;

    @JsonCreator
    public static ListingType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return SINGLE;
        }
        for (ListingType type : ListingType.values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid listing type: '" + value + "'. Allowed: SINGLE, BULK");
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
