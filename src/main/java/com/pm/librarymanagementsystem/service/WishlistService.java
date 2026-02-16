package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.wishlist.WishlistResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface WishlistService {

    WishlistResponse addWishlist(UUID bookId, String notes);

    void removeFromWishlist(UUID bookId);

    PageResponse<WishlistResponse> getMyWishlist(Pageable pageable);
}
