package com.scrapx.listing.dto;

import com.scrapx.listing.ListingCategory;
import com.scrapx.listing.ListingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class ListingRequest {

    @NotBlank(message = "Title cannot be blank")
    private String title;

    private String description;

    private ListingCategory category = ListingCategory.OTHER;

    private ListingType listingType = ListingType.SINGLE;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Double quantity;

    @NotBlank(message = "Unit cannot be blank (e.g. kg, pieces, bags)")
    private String unit;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be 0 or positive")
    private BigDecimal price;

    private String imageUrl;

    public ListingRequest() {
    }

    public ListingRequest(String title,
                          String description,
                          ListingCategory category,
                          ListingType listingType,
                          Double quantity,
                          String unit,
                          BigDecimal price,
                          String imageUrl) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.listingType = listingType;
        this.quantity = quantity;
        this.unit = unit;
        this.price = price;
        this.imageUrl = imageUrl;
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
}
