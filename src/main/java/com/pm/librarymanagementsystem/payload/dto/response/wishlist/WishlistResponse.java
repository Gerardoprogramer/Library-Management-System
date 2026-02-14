package com.pm.librarymanagementsystem.payload.dto.response.wishlist;

import com.pm.librarymanagementsystem.modal.Book;
import com.pm.librarymanagementsystem.modal.User;

import java.time.LocalDateTime;

public record WishlistResponse(
        Book book,
        User user,
        String notes,
        LocalDateTime addedAt
) {
}
