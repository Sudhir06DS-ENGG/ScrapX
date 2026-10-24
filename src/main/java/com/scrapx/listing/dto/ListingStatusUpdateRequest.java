package com.scrapx.listing.dto;

import com.scrapx.listing.ListingStatus;
import jakarta.validation.constraints.NotNull;

public class ListingStatusUpdateRequest {

    @NotNull(message = "Status cannot be null")
    private ListingStatus status;

    public ListingStatusUpdateRequest() {
    }

    public ListingStatusUpdateRequest(ListingStatus status) {
        this.status = status;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }
}
