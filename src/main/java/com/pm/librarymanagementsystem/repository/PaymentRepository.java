package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.modal.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
