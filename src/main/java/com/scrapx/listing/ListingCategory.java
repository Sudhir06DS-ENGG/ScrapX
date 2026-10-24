package com.scrapx.listing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ListingCategory {
    METAL,
    ELECTRONICS,
    BOOKS,
    CLOTHES,
    PLASTIC,
    PAPER,
    GLASS,
    VEHICLES,
    OTHER;

    @JsonCreator
    public static ListingCategory fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return OTHER;
        }
        for (ListingCategory category : ListingCategory.values()) {
            if (category.name().equalsIgnoreCase(value.trim())) {
                return category;
            }
        }
        return OTHER;
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
