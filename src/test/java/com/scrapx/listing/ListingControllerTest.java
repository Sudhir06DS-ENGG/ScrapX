package com.scrapx.listing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scrapx.auth.JwtService;
import com.scrapx.listing.dto.ListingRequest;
import com.scrapx.listing.dto.ListingStatusUpdateRequest;
import com.scrapx.user.User;
import com.scrapx.user.UserRepository;
import com.scrapx.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ListingControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User seller1;
    private String token1;

    private User seller2;
    private String token2;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        listingRepository.deleteAll();
        userRepository.deleteAll();

        // Create Seller 1
        seller1 = new User("Seller One", "seller1@example.com", passwordEncoder.encode("password123"), "9876543210", UserRole.Seller);
        seller1 = userRepository.save(seller1);
        token1 = jwtService.generateToken(seller1);

        // Create Seller 2
        seller2 = new User("Seller Two", "seller2@example.com", passwordEncoder.encode("password123"), "9876543211", UserRole.Seller);
        seller2 = userRepository.save(seller2);
        token2 = jwtService.generateToken(seller2);
    }

    @Test
    void testCreateListing() throws Exception {
        ListingRequest request = new ListingRequest(
                "Aluminum Cans",
                "Sorted clean aluminum beverage cans",
                ListingCategory.METAL,
                ListingType.BULK,
                15.0,
                "kg",
                new BigDecimal("1500.00"),
                "https://example.com/cans.jpg"
        );

        mockMvc.perform(post("/api/seller/listings")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Aluminum Cans"))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.sellerId").value(seller1.getId()));
    }

    @Test
    void testGetSellerListings() throws Exception {
        // Create 2 listings for seller1 and 1 for seller2
        Listing l1 = new Listing("Item 1", "Desc 1", ListingCategory.METAL, ListingType.SINGLE, 10.0, "kg", new BigDecimal("100"), null, seller1);
        Listing l2 = new Listing("Item 2", "Desc 2", ListingCategory.BOOKS, ListingType.BULK, 20.0, "kg", new BigDecimal("200"), null, seller1);
        Listing l3 = new Listing("Item 3", "Desc 3", ListingCategory.PLASTIC, ListingType.SINGLE, 5.0, "kg", new BigDecimal("50"), null, seller2);
        listingRepository.save(l1);
        listingRepository.save(l2);
        listingRepository.save(l3);

        mockMvc.perform(get("/api/seller/listings")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testGetOneListingOwnershipRule() throws Exception {
        Listing listing1 = listingRepository.save(
                new Listing("Item 1", "Desc 1", ListingCategory.METAL, ListingType.SINGLE, 10.0, "kg", new BigDecimal("100"), null, seller1)
        );

        // Seller 1 can fetch their listing
        mockMvc.perform(get("/api/seller/listings/" + listing1.getId())
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Item 1"));

        // Seller 2 CANNOT fetch seller 1's listing (403 Forbidden)
        mockMvc.perform(get("/api/seller/listings/" + listing1.getId())
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testUpdateListingOwnershipRule() throws Exception {
        Listing listing1 = listingRepository.save(
                new Listing("Original Title", "Desc", ListingCategory.METAL, ListingType.SINGLE, 10.0, "kg", new BigDecimal("100"), null, seller1)
        );

        ListingRequest updateReq = new ListingRequest(
                "Updated Title", "Updated Desc", ListingCategory.METAL, ListingType.SINGLE, 12.0, "kg", new BigDecimal("120"), null
        );

        // Seller 2 attempts to edit Seller 1's listing by changing ID in URL -> 403 Forbidden
        mockMvc.perform(put("/api/seller/listings/" + listing1.getId())
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        // Seller 1 updates their own listing -> 200 OK
        mockMvc.perform(put("/api/seller/listings/" + listing1.getId())
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"))
                .andExpect(jsonPath("$.data.quantity").value(12.0));
    }

    @Test
    void testDeleteListingSetsStatusToRemoved() throws Exception {
        Listing listing1 = listingRepository.save(
                new Listing("Item to delete", "Desc", ListingCategory.BOOKS, ListingType.SINGLE, 5.0, "kg", new BigDecimal("50"), null, seller1)
        );

        // Seller 2 attempts to delete Seller 1's listing -> 403 Forbidden
        mockMvc.perform(delete("/api/seller/listings/" + listing1.getId())
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden());

        // Seller 1 deletes their listing -> 200 OK
        mockMvc.perform(delete("/api/seller/listings/" + listing1.getId())
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REMOVED"));

        // Verify row is NOT deleted from DB, status is REMOVED for admin audit
        Listing inDb = listingRepository.findById(listing1.getId()).orElse(null);
        assertNotNull(inDb);
        assertEquals(ListingStatus.REMOVED, inDb.getStatus());
    }

    @Test
    void testChangeListingStatus() throws Exception {
        Listing listing1 = listingRepository.save(
                new Listing("Item", "Desc", ListingCategory.CLOTHES, ListingType.SINGLE, 10.0, "kg", new BigDecimal("100"), null, seller1)
        );

        ListingStatusUpdateRequest soldReq = new ListingStatusUpdateRequest(ListingStatus.SOLD);

        // Seller 2 attempts to change status -> 403 Forbidden
        mockMvc.perform(patch("/api/seller/listings/" + listing1.getId() + "/status")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(soldReq)))
                .andExpect(status().isForbidden());

        // Seller 1 marks AVAILABLE -> SOLD
        mockMvc.perform(patch("/api/seller/listings/" + listing1.getId() + "/status")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(soldReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SOLD"));

        // Terminal state: Cannot transition SOLD -> AVAILABLE
        ListingStatusUpdateRequest availReq = new ListingStatusUpdateRequest(ListingStatus.AVAILABLE);
        mockMvc.perform(patch("/api/seller/listings/" + listing1.getId() + "/status")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(availReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
