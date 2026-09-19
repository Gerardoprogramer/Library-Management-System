package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.modal.Book;

public interface ReservationQueueService {

    void promoteNextReservations(Book book);

    void expireAvailableReservations();
}