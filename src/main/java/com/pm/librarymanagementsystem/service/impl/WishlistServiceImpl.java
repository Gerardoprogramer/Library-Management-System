package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.exception.BusinessRuleException;
import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.mapper.WishlistMapper;
import com.pm.librarymanagementsystem.modal.Book;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.modal.Wishlist;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.wishlist.WishlistResponse;
import com.pm.librarymanagementsystem.repository.BookRepository;
import com.pm.librarymanagementsystem.repository.WishlistRepository;
import com.pm.librarymanagementsystem.service.UserService;
import com.pm.librarymanagementsystem.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserService userService;
    private final BookRepository bookRepository;

    @Override
    public WishlistResponse addWishlist(UUID bookId, String notes) {
        User user = userService.getCurrentUserEntity();

        Book book =  bookRepository.findById(bookId).orElseThrow(
                ()-> new NotFoundException("Libro no encontrado"));

        if(wishlistRepository.existsByUserIdAndBookId(user.getId(), book.getId())){
            throw new BusinessRuleException("El libro ya existe en la lista de deseos");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setBook(book);
        wishlist.setNotes(notes);
        return WishlistMapper.toResponse(wishlistRepository.save(wishlist));
    }

    @Override
    public void removeFromWishlist(UUID bookId) {
        User user = userService.getCurrentUserEntity();

        Wishlist wishlist = wishlistRepository.findByUserIdAndBookId(user.getId(), bookId);

        if(wishlist == null){
            throw new NotFoundException("No se encontro el libro en la lista de deseos");
        }

        wishlistRepository.delete(wishlist);
    }

    @Override
    public PageResponse<WishlistResponse> getMyWishlist(Pageable pageable) {
        User user = userService.getCurrentUserEntity();
        Page<Wishlist> wishlistPage = wishlistRepository.findByUserId(user.getId(), pageable);

        Page<WishlistResponse> mappedPage = wishlistPage.map(WishlistMapper::toResponse);

        return new PageResponse<>(
                mappedPage.getContent(),
                mappedPage.getNumber(),
                mappedPage.getSize(),
                mappedPage.getTotalElements(),
                mappedPage.getTotalPages(),
                mappedPage.isLast(),
                mappedPage.isFirst(),
                mappedPage.isEmpty()
        );
    }
}
