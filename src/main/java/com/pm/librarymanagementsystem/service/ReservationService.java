package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.payload.dto.request.reservation.ReservationRequest;
import com.pm.librarymanagementsystem.payload.dto.request.reservation.SearchReservationRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.reservation.ReservationResponse;
import org.springframework.data.domain.Pageable;

public interface ReservationService {

    ReservationResponse createReservation(ReservationRequest request);

    ReservationResponse createReservationForUser(Long UserId, ReservationRequest request);

    ReservationResponse cancelReservation(Long reservationId);

    ReservationResponse fulfillReservation(Long reservationId, Integer checkoutDays);

    PageResponse<ReservationResponse> searchReservations(Long userId, SearchReservationRequest request, Pageable pageable);

    PageResponse<ReservationResponse> getMyReservations(SearchReservationRequest request, Pageable pageable);
}
