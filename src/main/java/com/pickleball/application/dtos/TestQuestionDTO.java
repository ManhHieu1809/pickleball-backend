package com.pickleball.application.dtos;

import com.pickleball.domain.enums.QuestionCategory;
import lombok.*;

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
}
