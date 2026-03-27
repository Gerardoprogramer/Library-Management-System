package com.pm.librarymanagementsystem.controller.admin;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/reservations")
public class AdminReservationController {
    private final ReservationService reservationService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservationForUser(
            @PathVariable UUID userId,
            @Valid @RequestBody ReservationRequest request
    ){
        return ResponseEntity.ok(ApiResponse.success(
                "Reservación creada",
                reservationService.createReservationForUser(userId, request)
        ));
    }

    @PostMapping("/{id}/fulfill")
    public ResponseEntity<ApiResponse<ReservationResponse>> fulfillReservation(
            @PathVariable UUID id,
            @RequestParam Integer checkoutDays
    ){
        return ResponseEntity.ok(ApiResponse.success(
                "Reserva realizada",
                reservationService.fulfillReservation(id, checkoutDays)
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReservationResponse>>> searchReservations(
            @RequestParam UUID userId,
            @RequestParam(required = false)UUID bookId,
            @RequestParam(required = false) ReservationStatus status,
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
                reservationService.searchReservations(userId, request, pageable)
        ));
    }

}
