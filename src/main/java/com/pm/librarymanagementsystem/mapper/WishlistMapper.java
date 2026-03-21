package com.pm.librarymanagementsystem.mapper;

import com.pm.librarymanagementsystem.modal.Wishlist;
import com.pm.librarymanagementsystem.payload.dto.response.wishlist.WishlistResponse;

public class WishlistMapper {

    private WishlistMapper() {
    }


    /* =======================
       ENTITY → DTO
       ======================= */
    public static WishlistResponse toResponse(Wishlist wishlist) {

        return new WishlistResponse(
                wishlist.getId(),
                wishlist.getBook().getId(),
                wishlist.getBook().getTitle(),
                wishlist.getBook().getAuthor(),
                wishlist.getBook().getCoverImageUrl(),
                wishlist.getBook().getAvailableCopies(),
                wishlist.getNotes(),
                wishlist.getAddedAt()
        );
    }
}
