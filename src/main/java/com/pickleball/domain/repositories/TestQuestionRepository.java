package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.TestQuestion;
import com.pickleball.domain.enums.QuestionCategory;

import java.util.List;

public interface TestQuestionRepository {
    TestQuestion save(TestQuestion question);
    List<TestQuestion> findByCategory(QuestionCategory category);
    List<TestQuestion> findActiveQuestions();
    List<TestQuestion> findRandomQuestionsByCategory(QuestionCategory category, int limit);
    List<TestQuestion> findRandomQuestions(int limit);
    long countActiveQuestions();
}
