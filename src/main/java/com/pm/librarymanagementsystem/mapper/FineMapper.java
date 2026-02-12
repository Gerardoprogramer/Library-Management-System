package com.pm.librarymanagementsystem.mapper;

import com.pm.librarymanagementsystem.modal.Fine;
import com.pm.librarymanagementsystem.payload.dto.request.fine.FineRequest;
import com.pm.librarymanagementsystem.payload.dto.response.fine.FineResponse;

public class FineMapper {

    private FineMapper() {
    }

    /* =======================
       ENTITY → DTO
       ======================= */
    public static FineResponse toResponse(Fine fine) {

        if (fine == null) {
            return null;
        }

        return new FineResponse(
                fine.getId(),
                fine.getUser() != null ? fine.getUser().getId() : null,
                fine.getBookLoan() != null ? fine.getBookLoan().getId() : null,
                fine.getType(),
                fine.getAmount(),
                fine.getCurrency(),
                fine.getStatus(),
                fine.getReason(),
                fine.getNotes(),
                fine.getWaivedBy() != null ? fine.getWaivedBy().getId() : null,
                fine.getWaiverReason(),
                fine.getPaidAt(),
                fine.getProcessedBy() != null ? fine.getProcessedBy().getId() : null,
                fine.getTransactionId(),
                fine.getCreatedAt(),
                fine.getUpdatedAt()
        );
    }
}
