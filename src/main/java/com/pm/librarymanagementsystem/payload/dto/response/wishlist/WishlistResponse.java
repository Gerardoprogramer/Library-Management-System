package com.pm.librarymanagementsystem.payload.dto.response.wishlist;

import com.pm.librarymanagementsystem.modal.Book;
import com.pm.librarymanagementsystem.modal.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record WishlistResponse(
        UUID id,
        UUID bookId,
        String bookTitle,
        String bookAuthor,
        String bookCoverImageUrl,
        Integer availableCopies,
        String notes,
        LocalDateTime addedAt

) {
}
