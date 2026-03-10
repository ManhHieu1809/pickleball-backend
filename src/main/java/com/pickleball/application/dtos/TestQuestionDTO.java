package com.pickleball.application.dtos;

import com.pickleball.domain.enums.QuestionCategory;
import lombok.*;

/**
 * DTO for test questions - HIDES correct answer from player.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestQuestionDTO {
    private Long id;
    private QuestionCategory category;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    // NOTE: correctAnswer is NOT included - hidden from player
}
