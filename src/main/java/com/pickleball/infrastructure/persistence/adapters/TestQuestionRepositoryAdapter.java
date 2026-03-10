package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.TestQuestion;
import com.pickleball.domain.enums.QuestionCategory;
import com.pickleball.domain.repositories.TestQuestionRepository;
import com.pickleball.infrastructure.persistence.mappers.TestQuestionMapper;
import com.pickleball.infrastructure.persistence.repositories.TestQuestionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TestQuestionRepositoryAdapter implements TestQuestionRepository {

    private final TestQuestionJpaRepository jpaRepository;

    public TestQuestionRepositoryAdapter(TestQuestionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public TestQuestion save(TestQuestion question) {
        var entity = TestQuestionMapper.toEntity(question);
        var saved = jpaRepository.save(entity);
        return TestQuestionMapper.toDomain(saved);
    }

    @Override
    public List<TestQuestion> findByCategory(QuestionCategory category) {
        return jpaRepository.findByCategoryAndIsActiveTrue(category).stream()
                .map(TestQuestionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestQuestion> findActiveQuestions() {
        return jpaRepository.findByIsActiveTrue().stream()
                .map(TestQuestionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestQuestion> findRandomQuestionsByCategory(QuestionCategory category, int limit) {
        return jpaRepository.findRandomByCategory(category.name(), limit).stream()
                .map(TestQuestionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestQuestion> findRandomQuestions(int limit) {
        return jpaRepository.findRandomQuestions(limit).stream()
                .map(TestQuestionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countActiveQuestions() {
        return jpaRepository.countByIsActiveTrue();
    }
}
