package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.payload.dto.request.reservation.ReservationRequest;
import com.pm.librarymanagementsystem.payload.dto.request.reservation.SearchReservationRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.reservation.ReservationResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReservationService {

    ReservationResponse createReservation(ReservationRequest request);

    ReservationResponse createReservationForUser(UUID UserId, ReservationRequest request);

    ReservationResponse cancelReservation(UUID reservationId);

    ReservationResponse fulfillReservation(UUID reservationId, Integer checkoutDays);

    PageResponse<ReservationResponse> searchReservations(UUID userId, SearchReservationRequest request, Pageable pageable);

    PageResponse<ReservationResponse> getMyReservations(SearchReservationRequest request, Pageable pageable);
}
