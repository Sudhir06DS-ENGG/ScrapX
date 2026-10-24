package com.scrapx.listing;

import com.scrapx.common.ApiResponse;
import com.scrapx.listing.dto.ListingRequest;
import com.scrapx.listing.dto.ListingResponse;
import com.scrapx.listing.dto.ListingStatusUpdateRequest;
import com.scrapx.user.User;
import com.scrapx.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/seller/listings")
public class ListingController {

    private final ListingService listingService;
    private final UserService userService;

    public ListingController(ListingService listingService, UserService userService) {
        this.listingService = listingService;
        this.userService = userService;
    }

    /**
     * 1. POST /api/seller/listings : Create listing
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ListingResponse>> createListing(
            @Valid @RequestBody ListingRequest request,
            @AuthenticationPrincipal User user,
            Authentication authentication
    ) {
        User seller = resolveUser(user, authentication);
        ListingResponse response = listingService.createListing(request, seller);
        return new ResponseEntity<>(
                ApiResponse.success(response, "Scrap listing created successfully"),
                HttpStatus.CREATED
        );
    }

    /**
     * 2. GET /api/seller/listings : Get seller's listings
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ListingResponse>>> getSellerListings(
            @AuthenticationPrincipal User user,
            Authentication authentication
    ) {
        User seller = resolveUser(user, authentication);
        List<ListingResponse> listings = listingService.getSellerListings(seller);
        return ResponseEntity.ok(ApiResponse.success(listings, "Seller listings retrieved successfully"));
    }

    /**
     * 3. GET /api/seller/listings/{id} : Get one listing
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ListingResponse>> getListingById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            Authentication authentication
    ) {
        User seller = resolveUser(user, authentication);
        ListingResponse response = listingService.getSellerListingById(id, seller);
        return ResponseEntity.ok(ApiResponse.success(response, "Listing retrieved successfully"));
    }

    /**
     * 4. PUT /api/seller/listings/{id} : Update listing
     * Ownership check: Ensures the listing belongs to the authenticated seller.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ListingResponse>> updateListing(
            @PathVariable Long id,
            @Valid @RequestBody ListingRequest request,
            @AuthenticationPrincipal User user,
            Authentication authentication
    ) {
        User seller = resolveUser(user, authentication);
        ListingResponse response = listingService.updateListing(id, request, seller);
        return ResponseEntity.ok(ApiResponse.success(response, "Listing updated successfully"));
    }

    /**
     * 5. DELETE /api/seller/listings/{id} : Remove listing
     * Ownership check & soft delete: sets status to REMOVED rather than deleting row.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ListingResponse>> removeListing(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            Authentication authentication
    ) {
        User seller = resolveUser(user, authentication);
        ListingResponse response = listingService.deleteListing(id, seller);
        return ResponseEntity.ok(ApiResponse.success(response, "Listing removed successfully"));
    }

    /**
     * 6. PATCH /api/seller/listings/{id}/status : Change listing status
     * Ownership check & state machine rules.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ListingResponse>> changeListingStatus(
            @PathVariable Long id,
            @Valid @RequestBody ListingStatusUpdateRequest request,
            @AuthenticationPrincipal User user,
            Authentication authentication
    ) {
        User seller = resolveUser(user, authentication);
        ListingResponse response = listingService.updateListingStatus(id, request.getStatus(), seller);
        return ResponseEntity.ok(ApiResponse.success(response, "Listing status updated to " + request.getStatus()));
    }

    // --- Exception Handlers ---

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        HttpStatus status = ex.getMessage() != null && ex.getMessage().contains("not found")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(errorMsg));
    }

    private User resolveUser(User user, Authentication authentication) {
        if (user != null) {
            return user;
        }
        if (authentication != null && authentication.getName() != null) {
            return userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        }
        throw new AccessDeniedException("Authentication required");
    }
}
