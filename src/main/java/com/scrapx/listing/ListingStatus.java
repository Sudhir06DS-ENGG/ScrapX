package com.scrapx.listing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ListingStatus {
    AVAILABLE,
    BOOKED,
    SOLD,
    REMOVED;

    /**
     * State transition rules:
     * - AVAILABLE -> BOOKED (Buyer accepts listing)
     * - AVAILABLE -> SOLD (Seller marks sold)
     * - AVAILABLE -> REMOVED (Seller/Admin removes)
     * - BOOKED -> SOLD (Transaction completed)
     * - BOOKED -> AVAILABLE (Booking cancelled)
     * - SOLD / REMOVED -> terminal states (no transitions allowed)
     */
    public boolean canTransitionTo(ListingStatus newStatus) {
        if (newStatus == null || this == newStatus) {
            return false;
        }

        return switch (this) {
            case AVAILABLE -> newStatus == BOOKED || newStatus == SOLD || newStatus == REMOVED;
            case BOOKED -> newStatus == SOLD || newStatus == AVAILABLE;
            case SOLD, REMOVED -> false;
        };
    }

    public void validateTransitionTo(ListingStatus newStatus) {
        if (!canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("Invalid status transition from '%s' to '%s'", this, newStatus)
            );
        }
    }

    @JsonCreator
    public static ListingStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return AVAILABLE;
        }
        for (ListingStatus status : ListingStatus.values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid listing status: '" + value + "'. Allowed: AVAILABLE, BOOKED, SOLD, REMOVED");
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
