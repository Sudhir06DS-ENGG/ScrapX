package com.scrapx.listing.dto;

import com.scrapx.listing.Listing;
import com.scrapx.listing.ListingCategory;
import com.scrapx.listing.ListingStatus;
import com.scrapx.listing.ListingType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ListingResponse {

    private Long id;
    private String title;
    private String description;
    private ListingCategory category;
    private ListingType listingType;
    private Double quantity;
    private String unit;
    private BigDecimal price;
    private String imageUrl;
    private ListingStatus status;
    private Long sellerId;
    private String sellerName;
    private String sellerEmail;
    private String sellerPhoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ListingResponse() {
    }

    public static ListingResponse fromListing(Listing listing) {
        if (listing == null) {
            return null;
        }

        ListingResponse response = new ListingResponse();
        response.setId(listing.getId());
        response.setTitle(listing.getTitle());
        response.setDescription(listing.getDescription());
        response.setCategory(listing.getCategory());
        response.setListingType(listing.getListingType());
        response.setQuantity(listing.getQuantity());
        response.setUnit(listing.getUnit());
        response.setPrice(listing.getPrice());
        response.setImageUrl(listing.getImageUrl());
        response.setStatus(listing.getStatus());
        response.setCreatedAt(listing.getCreatedAt());
        response.setUpdatedAt(listing.getUpdatedAt());

        if (listing.getSeller() != null) {
            response.setSellerId(listing.getSeller().getId());
            response.setSellerName(listing.getSeller().getName());
            response.setSellerEmail(listing.getSeller().getEmail());
            response.setSellerPhoneNumber(listing.getSeller().getPhoneNumber());
        }

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ListingCategory getCategory() {
        return category;
    }

    public void setCategory(ListingCategory category) {
        this.category = category;
    }

    public ListingType getListingType() {
        return listingType;
    }

    public void setListingType(ListingType listingType) {
        this.listingType = listingType;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerEmail() {
        return sellerEmail;
    }

    public void setSellerEmail(String sellerEmail) {
        this.sellerEmail = sellerEmail;
    }

    public String getSellerPhoneNumber() {
        return sellerPhoneNumber;
    }

    public void setSellerPhoneNumber(String sellerPhoneNumber) {
        this.sellerPhoneNumber = sellerPhoneNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
