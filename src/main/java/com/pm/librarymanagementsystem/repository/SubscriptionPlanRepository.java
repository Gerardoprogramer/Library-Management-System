package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.modal.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    Boolean existsByPlanCode(String planCode);
}
