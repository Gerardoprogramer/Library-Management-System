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
                wishlist.getBook(),
                wishlist.getUser(),
                wishlist.getNotes(),
                wishlist.getAddedAt()
        );
    }
}
