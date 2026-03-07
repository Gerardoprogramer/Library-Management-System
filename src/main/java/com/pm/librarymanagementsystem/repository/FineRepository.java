package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.domain.FineStatus;
import com.pm.librarymanagementsystem.domain.FineType;
import com.pm.librarymanagementsystem.modal.Fine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface FineRepository extends JpaRepository<Fine, UUID> {

    @Query("""
    select f from Fine f
    where (:userId is null or f.user.id = :userId)
    and (:status is null or f.status = :status)
    and (:type is null or f.type = :type)
    order by f.createdAt desc
""")
    Page<Fine> findAllWithFilters(
            @Param("userId") UUID userId,
            @Param("status")FineStatus status,
            @Param("type")FineType type,
            Pageable pageable
            );
}
