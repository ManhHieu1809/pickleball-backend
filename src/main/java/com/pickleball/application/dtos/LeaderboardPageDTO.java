package com.pickleball.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardPageDTO {
    private List<LeaderboardEntryDTO> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}

