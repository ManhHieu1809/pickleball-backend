package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.TestQuestion;
import com.pickleball.infrastructure.persistence.entities.TestQuestionEntity;

public class TestQuestionMapper {

    public static TestQuestion toDomain(TestQuestionEntity entity) {
        if (entity == null) return null;
        return TestQuestion.builder()
                .id(entity.getId())
                .category(entity.getCategory())
                .questionText(entity.getQuestionText())
                .optionA(entity.getOptionA())
                .optionB(entity.getOptionB())
                .optionC(entity.getOptionC())
                .optionD(entity.getOptionD())
                .correctAnswer(entity.getCorrectAnswer())
                .difficulty(entity.getDifficulty())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static TestQuestionEntity toEntity(TestQuestion domain) {
        if (domain == null) return null;
        return TestQuestionEntity.builder()
                .id(domain.getId())
                .category(domain.getCategory())
                .questionText(domain.getQuestionText())
                .optionA(domain.getOptionA())
                .optionB(domain.getOptionB())
                .optionC(domain.getOptionC())
                .optionD(domain.getOptionD())
                .correctAnswer(domain.getCorrectAnswer())
                .difficulty(domain.getDifficulty())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
