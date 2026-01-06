package com.pm.librarymanagementsystem.payload.dto.response.book;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,

        long totalElements,
        int totalPages,

        boolean last,
        boolean first,
        boolean empty
) { }
