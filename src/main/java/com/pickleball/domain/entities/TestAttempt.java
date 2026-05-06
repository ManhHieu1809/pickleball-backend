package com.pickleball.domain.entities;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestAttempt {
    private Long id;
    private Long userId;
    private Integer score;
    @Builder.Default
    private Integer totalQuestions = 10;
    @Builder.Default
    private Boolean passed = false;
    private String answers;
    private LocalDateTime attemptedAt;

    public static final int PASSING_SCORE = 9;
    public static final int TOTAL_QUESTIONS = 10;

    public boolean hasPassed() {
        return score != null && score >= PASSING_SCORE;
    }

    public void calculateResult(int correctCount) {
        this.score = correctCount;
        this.passed = correctCount >= PASSING_SCORE;
        this.attemptedAt = LocalDateTime.now();
    }
}
