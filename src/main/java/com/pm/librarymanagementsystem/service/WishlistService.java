package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.wishlist.WishlistResponse;
import org.springframework.data.domain.Pageable;

public interface WishlistService {

    WishlistResponse addWishlist(Long bookId, String notes);

    void removeFromWishlist(Long bookId);

    PageResponse<WishlistResponse> getMyWishlist(Pageable pageable);
}
