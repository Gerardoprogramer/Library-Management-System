package com.pm.librarymanagementsystem.controller.users;

import com.pm.librarymanagementsystem.domain.ReservationStatus;
import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.request.reservation.ReservationRequest;
import com.pm.librarymanagementsystem.payload.dto.request.reservation.SearchReservationRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.reservation.ReservationResponse;
import com.pm.librarymanagementsystem.service.ReservationService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @Valid @RequestBody ReservationRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                "Reservación creada",
                reservationService.createReservation(request)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(
            @PathVariable UUID id
    ){
        return ResponseEntity.ok(ApiResponse.success(
                "Reservación cancelada",
                reservationService.cancelReservation(id)
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<ReservationResponse>>> getMyReservations(
            @RequestParam(required = false)UUID bookId,
            @RequestParam(required = false)ReservationStatus status,
            @RequestParam(required = false)Boolean activeOnly,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ){
        SearchReservationRequest request = new SearchReservationRequest(bookId,status,activeOnly);

        return ResponseEntity.ok(ApiResponse.success(
                "Listado de reservaciónes",
                reservationService.getMyReservations(request, pageable)
        ));
    }

    @GetMapping("/queue/{bookId}")
    public ResponseEntity<ApiResponse<Long>> positionQueue(
            @PathVariable UUID bookId
    ){
        return ResponseEntity.ok(ApiResponse.success(
                "La Queue del libro es",
                reservationService.positionUserForBook(bookId)
        ));
    }
}
