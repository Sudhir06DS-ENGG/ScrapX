package com.scrapx.listing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListingStatusTest {

    @Test
    void testAvailableTransitions() {
        ListingStatus status = ListingStatus.AVAILABLE;

        // Valid transitions from AVAILABLE
        assertTrue(status.canTransitionTo(ListingStatus.BOOKED), "AVAILABLE -> BOOKED should be valid");
        assertTrue(status.canTransitionTo(ListingStatus.REMOVED), "AVAILABLE -> REMOVED should be valid");
        assertTrue(status.canTransitionTo(ListingStatus.SOLD), "AVAILABLE -> SOLD should be valid");

        // Invalid transitions
        assertFalse(status.canTransitionTo(ListingStatus.AVAILABLE), "Self transition should be invalid");
        assertFalse(status.canTransitionTo(null), "Null transition should be invalid");

        assertDoesNotThrow(() -> status.validateTransitionTo(ListingStatus.BOOKED));
        assertDoesNotThrow(() -> status.validateTransitionTo(ListingStatus.REMOVED));
        assertDoesNotThrow(() -> status.validateTransitionTo(ListingStatus.SOLD));
    }

    @Test
    void testBookedTransitions() {
        ListingStatus status = ListingStatus.BOOKED;

        // Valid transitions from BOOKED
        assertTrue(status.canTransitionTo(ListingStatus.SOLD), "BOOKED -> SOLD should be valid");
        assertTrue(status.canTransitionTo(ListingStatus.AVAILABLE), "BOOKED -> AVAILABLE should be valid");

        // Invalid transitions from BOOKED
        assertFalse(status.canTransitionTo(ListingStatus.REMOVED), "BOOKED -> REMOVED directly should be invalid");
        assertFalse(status.canTransitionTo(ListingStatus.BOOKED), "Self transition should be invalid");

        assertDoesNotThrow(() -> status.validateTransitionTo(ListingStatus.SOLD));
        assertDoesNotThrow(() -> status.validateTransitionTo(ListingStatus.AVAILABLE));
        assertThrows(IllegalStateException.class, () -> status.validateTransitionTo(ListingStatus.REMOVED));
    }

    @Test
    void testSoldIsTerminal() {
        ListingStatus status = ListingStatus.SOLD;

        assertFalse(status.canTransitionTo(ListingStatus.AVAILABLE));
        assertFalse(status.canTransitionTo(ListingStatus.BOOKED));
        assertFalse(status.canTransitionTo(ListingStatus.REMOVED));

        assertThrows(IllegalStateException.class, () -> status.validateTransitionTo(ListingStatus.AVAILABLE));
    }

    @Test
    void testRemovedIsTerminal() {
        ListingStatus status = ListingStatus.REMOVED;

        assertFalse(status.canTransitionTo(ListingStatus.AVAILABLE));
        assertFalse(status.canTransitionTo(ListingStatus.BOOKED));
        assertFalse(status.canTransitionTo(ListingStatus.SOLD));

        assertThrows(IllegalStateException.class, () -> status.validateTransitionTo(ListingStatus.AVAILABLE));
    }
}
