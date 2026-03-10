package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.TestAttempt;
import com.pickleball.infrastructure.persistence.entities.TestAttemptEntity;

public class TestAttemptMapper {

    public static TestAttempt toDomain(TestAttemptEntity entity) {
        if (entity == null) return null;
        return TestAttempt.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .score(entity.getScore())
                .totalQuestions(entity.getTotalQuestions())
                .passed(entity.getPassed())
                .answers(entity.getAnswers())
                .attemptedAt(entity.getAttemptedAt())
                .build();
    }

    public static TestAttemptEntity toEntity(TestAttempt domain) {
        if (domain == null) return null;
        return TestAttemptEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .score(domain.getScore())
                .totalQuestions(domain.getTotalQuestions())
                .passed(domain.getPassed())
                .answers(domain.getAnswers())
                .attemptedAt(domain.getAttemptedAt())
                .build();
    }
}
