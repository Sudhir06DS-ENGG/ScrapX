package com.scrapx.listing;

import com.scrapx.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findByStatus(ListingStatus status);

    List<Listing> findBySeller(User seller);

    List<Listing> findBySellerId(Long sellerId);

    List<Listing> findByCategoryAndStatus(ListingCategory category, ListingStatus status);

    List<Listing> findBySellerIdAndStatus(Long sellerId, ListingStatus status);
}
