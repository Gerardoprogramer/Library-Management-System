package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.exception.ConflictException;
import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.mapper.SubscriptionPlanMapper;
import com.pm.librarymanagementsystem.modal.SubscriptionPlan;
import com.pm.librarymanagementsystem.payload.dto.response.SubscriptionPlan.SubscriptionPlanResponse;
import com.pm.librarymanagementsystem.payload.dto.response.user.UserResponse;
import com.pm.librarymanagementsystem.payload.dto.resquest.SubscriptionPlan.CreateSubscriptionPlanRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.SubscriptionPlan.UpdateSubscriptionPlanRequest;
import com.pm.librarymanagementsystem.repository.SubscriptionPlanRepository;
import com.pm.librarymanagementsystem.service.SubscriptionPlanService;
import com.pm.librarymanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserService userService;

    @Override
    public SubscriptionPlanResponse createSubscriptionPlan(CreateSubscriptionPlanRequest request) {

        if(subscriptionPlanRepository.existsByPlanCode(request.planCode())){
            throw new ConflictException("El código del plan ya existe.");
        }

        SubscriptionPlan plan = SubscriptionPlanMapper.toEntity(request);
        UserResponse currentUser = userService.getCurrentUser();
        plan.setCreatedBy(currentUser.fullName());
        plan.setUpdatedBy(currentUser.fullName());

        return SubscriptionPlanMapper.toResponse(subscriptionPlanRepository.save(plan));
    }

    @Override
    public SubscriptionPlanResponse updateSubscriptionPlan(long id, UpdateSubscriptionPlanRequest request) {

        SubscriptionPlan plan = subscriptionPlanRepository.findById(id).orElseThrow(
                ()-> new NotFoundException("El plan de Subscripción no existe")
        );

        SubscriptionPlanMapper.updateEntity(plan, request);
        UserResponse currentUser = userService.getCurrentUser();
        plan.setUpdatedBy(currentUser.fullName());

        return SubscriptionPlanMapper.toResponse(subscriptionPlanRepository.save(plan));
    }

    @Override
    public void deleteSubscriptionPlan(Long id) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id).orElseThrow(
                ()-> new NotFoundException("El plan de Subscripción no existe")
        );

        subscriptionPlanRepository.delete(plan);
    }

    @Override
    public Page<SubscriptionPlanResponse> getAllSubscriptionPlans(int page, int size) {
            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by("createdAt").descending()
            );

            return subscriptionPlanRepository.findAll(pageable)
                    .map(SubscriptionPlanMapper::toResponse);
        }
    }

