package com.scrapx.listing;

import com.scrapx.listing.dto.ListingRequest;
import com.scrapx.listing.dto.ListingResponse;
import com.scrapx.user.User;
import com.scrapx.user.UserRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ListingService {

    private final ListingRepository listingRepository;

    public ListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    /**
     * 1. POST /api/seller/listings : Create listing
     * Creates a scrap listing with status AVAILABLE and seller set to authenticated user.
     */
    public ListingResponse createListing(ListingRequest request, User seller) {
        if (seller == null) {
            throw new IllegalArgumentException("Seller cannot be null");
        }

        Listing listing = new Listing(
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getListingType(),
                request.getQuantity(),
                request.getUnit(),
                request.getPrice(),
                request.getImageUrl(),
                seller
        );

        Listing saved = listingRepository.save(listing);
        return ListingResponse.fromListing(saved);
    }

    /**
     * 2. GET /api/seller/listings : Get seller's listings
     * Returns all listings belonging to the authenticated seller.
     */
    @Transactional(readOnly = true)
    public List<ListingResponse> getSellerListings(User seller) {
        return listingRepository.findBySeller(seller)
                .stream()
                .map(ListingResponse::fromListing)
                .collect(Collectors.toList());
    }

    /**
     * 3. GET /api/seller/listings/{id} : Get one listing
     * Enforces ownership rule: verifies the listing belongs to the authenticated user.
     */
    @Transactional(readOnly = true)
    public ListingResponse getSellerListingById(Long id, User seller) {
        Listing listing = findListingOrThrow(id);
        verifyOwnership(listing, seller);
        return ListingResponse.fromListing(listing);
    }

    /**
     * 4. PUT /api/seller/listings/{id} : Update listing
     * Enforces ownership rule:
     * 1. Find the listing by ID.
     * 2. Get the authenticated user.
     * 3. Verify that the listing belongs to that user.
     * 4. Only then update it.
     */
    public ListingResponse updateListing(Long id, ListingRequest request, User seller) {
        Listing listing = findListingOrThrow(id);
        verifyOwnership(listing, seller);

        // Cannot edit already SOLD or REMOVED listings
        if (listing.getStatus() == ListingStatus.SOLD || listing.getStatus() == ListingStatus.REMOVED) {
            throw new IllegalStateException("Cannot edit listing because it is already " + listing.getStatus());
        }

        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listing.setCategory(request.getCategory() != null ? request.getCategory() : ListingCategory.OTHER);
        listing.setListingType(request.getListingType() != null ? request.getListingType() : ListingType.SINGLE);
        listing.setQuantity(request.getQuantity());
        listing.setUnit(request.getUnit());
        listing.setPrice(request.getPrice());
        listing.setImageUrl(request.getImageUrl());

        Listing updated = listingRepository.save(listing);
        return ListingResponse.fromListing(updated);
    }

    /**
     * 5. DELETE /api/seller/listings/{id} : Remove listing
     * Enforces ownership rule and soft-deletes by setting status to REMOVED
     * rather than physically deleting the row to preserve listing history.
     */
    public ListingResponse deleteListing(Long id, User seller) {
        Listing listing = findListingOrThrow(id);
        verifyOwnership(listing, seller);

        // Transition status to REMOVED
        listing.transitionTo(ListingStatus.REMOVED);

        Listing updated = listingRepository.save(listing);
        return ListingResponse.fromListing(updated);
    }

    /**
     * 6. PATCH /api/seller/listings/{id}/status : Change listing status
     * Enforces ownership and state machine rules:
     * - AVAILABLE -> BOOKED, SOLD, REMOVED
     * - BOOKED -> SOLD, AVAILABLE
     */
    public ListingResponse updateListingStatus(Long id, ListingStatus newStatus, User actor) {
        Listing listing = findListingOrThrow(id);
        verifyOwnership(listing, actor);

        // Apply state transition rules
        listing.transitionTo(newStatus);

        Listing updated = listingRepository.save(listing);
        return ListingResponse.fromListing(updated);
    }

    // --- General browsing methods ---

    @Transactional(readOnly = true)
    public ListingResponse getListingById(Long id) {
        Listing listing = findListingOrThrow(id);
        return ListingResponse.fromListing(listing);
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> getAllAvailableListings() {
        return listingRepository.findByStatus(ListingStatus.AVAILABLE)
                .stream()
                .map(ListingResponse::fromListing)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> getAvailableListingsByCategory(ListingCategory category) {
        return listingRepository.findByCategoryAndStatus(category, ListingStatus.AVAILABLE)
                .stream()
                .map(ListingResponse::fromListing)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> getListingsBySeller(User seller) {
        return getSellerListings(seller);
    }

    /**
     * Ownership verification helper:
     * Ensures the listing belongs to the user, or user is an Admin.
     * Prevents a seller from accessing/editing another seller's listing.
     */
    private void verifyOwnership(Listing listing, User user) {
        if (user == null || listing.getSeller() == null) {
            throw new AccessDeniedException("Access denied: You do not have permission to access this listing");
        }

        boolean isOwner = listing.getSeller().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == UserRole.Admin;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Access denied: You do not own this listing");
        }
    }

    private Listing findListingOrThrow(Long id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + id));
    }
}
