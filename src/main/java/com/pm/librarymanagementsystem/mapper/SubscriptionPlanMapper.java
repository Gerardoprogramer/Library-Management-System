package com.pm.librarymanagementsystem.mapper;

import com.pm.librarymanagementsystem.modal.SubscriptionPlan;
import com.pm.librarymanagementsystem.payload.dto.response.SubscriptionPlan.SubscriptionPlanResponse;
import com.pm.librarymanagementsystem.payload.dto.resquest.SubscriptionPlan.CreateSubscriptionPlanRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.SubscriptionPlan.UpdateSubscriptionPlanRequest;

import java.util.function.Consumer;

public class SubscriptionPlanMapper {

    private SubscriptionPlanMapper() {
    }

    /* =======================
       DTO → ENTITY
       ======================= */

    public static SubscriptionPlan toEntity(CreateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setPlanCode(request.planCode());
        plan.setName(request.name());
        plan.setDescription(request.description());
        plan.setDurationDays(request.durationDays());
        plan.setPrice(request.price());
        plan.setCurrency(request.currency());
        plan.setMaxBooksAllowed(request.maxBooksAllowed());
        plan.setMaxDaysPerBook(request.maxDaysPerBook());

        plan.setDisplayOrder(
                request.displayOrder() != null ? request.displayOrder() : 0
        );
        plan.setActive(
                request.active() != null ? request.active() : true
        );
        plan.setFeatured(
                request.featured() != null ? request.featured() : false
        );

        plan.setBadgeText(request.badgeText());
        plan.setAdminNotes(request.adminNotes());

        return plan;
    }

    public static void updateEntity(SubscriptionPlan plan, UpdateSubscriptionPlanRequest request) {
        setIfNotNull(request.name(), plan::setName);
        setIfNotNull(request.description(), plan::setDescription);
        setIfNotNull(request.durationDays(), plan::setDurationDays);
        setIfNotNull(request.price(), plan::setPrice);
        setIfNotNull(request.currency(), plan::setCurrency);
        setIfNotNull(request.maxBooksAllowed(), plan::setMaxBooksAllowed);
        setIfNotNull(request.maxDaysPerBook(), plan::setMaxDaysPerBook);
        setIfNotNull(request.displayOrder(), plan::setDisplayOrder);
        setIfNotNull(request.active(), plan::setActive);
        setIfNotNull(request.featured(), plan::setFeatured);
        setIfNotNull(request.badgeText(), plan::setBadgeText);
        setIfNotNull(request.adminNotes(), plan::setAdminNotes);
    }

    /* =======================
       ENTITY → DTO
       ======================= */

    public static SubscriptionPlanResponse toResponse(SubscriptionPlan plan) {
        return new SubscriptionPlanResponse(
                plan.getId(),
                plan.getPlanCode(),
                plan.getName(),
                plan.getDescription(),
                plan.getDurationDays(),
                plan.getPrice(),
                plan.getCurrency(),
                plan.getMaxBooksAllowed(),
                plan.getMaxDaysPerBook(),
                plan.getDisplayOrder(),
                plan.getActive(),
                plan.getFeatured(),
                plan.getBadgeText(),
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }

    private static <T> void setIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
