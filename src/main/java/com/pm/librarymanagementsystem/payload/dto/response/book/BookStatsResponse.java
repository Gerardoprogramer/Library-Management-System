package com.pm.librarymanagementsystem.payload.dto.response.book;

public class BookStatsResponse {
    public long totalActiveBooks;
    public long totalAvailableBooks;

    public BookStatsResponse(long totalActive, long totalAvailable){
        this.totalActiveBooks = totalActive;
        this.totalAvailableBooks = totalAvailable;
    }
}
