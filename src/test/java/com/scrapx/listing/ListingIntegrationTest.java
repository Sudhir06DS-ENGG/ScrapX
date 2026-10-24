package com.scrapx.listing;

import com.scrapx.listing.dto.ListingRequest;
import com.scrapx.listing.dto.ListingResponse;
import com.scrapx.user.User;
import com.scrapx.user.UserRepository;
import com.scrapx.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ListingIntegrationTest {

    @Autowired
    private ListingService listingService;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserRepository userRepository;

    private User seller;

    @BeforeEach
    void setUp() {
        listingRepository.deleteAll();
        userRepository.deleteAll();

        seller = new User(
                "John Seller",
                "seller@example.com",
                "encoded_pw",
                "1234567890",
                UserRole.Seller
        );
        seller = userRepository.save(seller);
    }

    @Test
    void testCreateListingDefaultsToAvailable() {
        ListingRequest request = new ListingRequest(
                "Old Copper Cables",
                "High quality scrap copper wire, stripped and unstripped",
                ListingCategory.METAL,
                ListingType.BULK,
                25.5,
                "kg",
                new BigDecimal("12750.00"),
                "https://images.scrapx.com/copper1.jpg"
        );

        ListingResponse response = listingService.createListing(request, seller);

        assertNotNull(response.getId());
        assertEquals("Old Copper Cables", response.getTitle());
        assertEquals(ListingStatus.AVAILABLE, response.getStatus());
        assertEquals(ListingCategory.METAL, response.getCategory());
        assertEquals(ListingType.BULK, response.getListingType());
        assertEquals(25.5, response.getQuantity());
        assertEquals("kg", response.getUnit());
        assertEquals(new BigDecimal("12750.00"), response.getPrice());
        assertEquals(seller.getId(), response.getSellerId());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    void testListingStatusFlowAvailableToBookedToSold() {
        ListingRequest request = new ListingRequest(
                "Used CRT Monitors",
                "5 old CRT monitors for electronic recycling",
                ListingCategory.ELECTRONICS,
                ListingType.SINGLE,
                5.0,
                "pieces",
                new BigDecimal("500.00"),
                null
        );

        ListingResponse created = listingService.createListing(request, seller);
        assertEquals(ListingStatus.AVAILABLE, created.getStatus());

        // 1. AVAILABLE -> BOOKED (Buyer accepts listing)
        ListingResponse booked = listingService.updateListingStatus(created.getId(), ListingStatus.BOOKED, seller);
        assertEquals(ListingStatus.BOOKED, booked.getStatus());

        // 2. BOOKED -> SOLD (Transaction completed)
        ListingResponse sold = listingService.updateListingStatus(booked.getId(), ListingStatus.SOLD, seller);
        assertEquals(ListingStatus.SOLD, sold.getStatus());

        // 3. SOLD is terminal: cannot go back to AVAILABLE
        assertThrows(IllegalStateException.class, () ->
                listingService.updateListingStatus(sold.getId(), ListingStatus.AVAILABLE, seller)
        );
    }

    @Test
    void testListingStatusFlowAvailableToBookedToAvailableToRemoved() {
        ListingRequest request = new ListingRequest(
                "Cardboard Cartons",
                "Flat-packed cardboard boxes",
                ListingCategory.PAPER,
                ListingType.BULK,
                50.0,
                "kg",
                new BigDecimal("400.00"),
                null
        );

        ListingResponse created = listingService.createListing(request, seller);

        // AVAILABLE -> BOOKED
        ListingResponse booked = listingService.updateListingStatus(created.getId(), ListingStatus.BOOKED, seller);
        assertEquals(ListingStatus.BOOKED, booked.getStatus());

        // BOOKED -> AVAILABLE (Booking cancelled)
        ListingResponse cancelled = listingService.updateListingStatus(booked.getId(), ListingStatus.AVAILABLE, seller);
        assertEquals(ListingStatus.AVAILABLE, cancelled.getStatus());

        // AVAILABLE -> REMOVED (Seller removes listing)
        ListingResponse removed = listingService.updateListingStatus(cancelled.getId(), ListingStatus.REMOVED, seller);
        assertEquals(ListingStatus.REMOVED, removed.getStatus());

        // REMOVED is terminal
        assertThrows(IllegalStateException.class, () ->
                listingService.updateListingStatus(removed.getId(), ListingStatus.AVAILABLE, seller)
        );
    }

    @Test
    void testFindAvailableListings() {
        ListingRequest r1 = new ListingRequest("Iron Rods", "Rusty iron scrap", ListingCategory.METAL, ListingType.SINGLE, 10.0, "kg", new BigDecimal("300.00"), null);
        ListingRequest r2 = new ListingRequest("Old Books", "Textbooks and magazines", ListingCategory.BOOKS, ListingType.BULK, 20.0, "kg", new BigDecimal("200.00"), null);

        ListingResponse l1 = listingService.createListing(r1, seller);
        ListingResponse l2 = listingService.createListing(r2, seller);

        // Mark l2 as SOLD
        listingService.updateListingStatus(l2.getId(), ListingStatus.SOLD, seller);

        List<ListingResponse> available = listingService.getAllAvailableListings();
        assertEquals(1, available.size());
        assertEquals(l1.getId(), available.get(0).getId());
    }
}
