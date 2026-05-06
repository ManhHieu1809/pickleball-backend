package com.pickleball.domain.entities;

import com.pickleball.domain.enums.QuestionCategory;
import com.pickleball.domain.enums.QuestionDifficulty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestQuestion {
    private Long id;
    private QuestionCategory category;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    @Builder.Default
    private QuestionDifficulty difficulty = QuestionDifficulty.MEDIUM;
    @Builder.Default
    private Boolean isActive = true;
    private LocalDateTime createdAt;

    public boolean isCorrect(String answer) {
        return correctAnswer != null && correctAnswer.equalsIgnoreCase(answer);
    }
}
