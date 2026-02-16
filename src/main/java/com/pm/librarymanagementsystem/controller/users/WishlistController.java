package com.pm.librarymanagementsystem.controller.users;

import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.wishlist.WishlistResponse;
import com.pm.librarymanagementsystem.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{bookId}")
    public ResponseEntity<ApiResponse<WishlistResponse>> addWishlist(
            @PathVariable UUID bookId,
            @RequestParam(required = false) String notes
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Libro agregado a la lista de deseos",
                        wishlistService.addWishlist(bookId, notes)
                ));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @PathVariable UUID bookId
    ){
        wishlistService.removeFromWishlist(bookId);
        return ResponseEntity.ok(ApiResponse.success(
                "Libro eliminado de la lista de deseos"
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<WishlistResponse>>> getMyWishlist(
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable){
        return ResponseEntity.ok(ApiResponse.success(
                "Listados de deseos",
                wishlistService.getMyWishlist(pageable)
        ));
    }
}
