package com.pickleball.application.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefereeTestResultDTO {
    private Long attemptId;
    private Long userId;
    private Integer score;
    private Integer totalQuestions;
    private Boolean passed;
    private LocalDateTime attemptedAt;
    private String message;
}
