package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.modal.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    Boolean existsByPlanCode(String planCode);
}
