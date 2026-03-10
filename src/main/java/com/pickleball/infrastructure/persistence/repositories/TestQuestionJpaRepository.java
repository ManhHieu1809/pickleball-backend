package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.TestQuestionEntity;
import com.pickleball.domain.enums.QuestionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestQuestionJpaRepository extends JpaRepository<TestQuestionEntity, Long> {
    List<TestQuestionEntity> findByCategoryAndIsActiveTrue(QuestionCategory category);

    List<TestQuestionEntity> findByIsActiveTrue();

    @Query(value = "SELECT * FROM referee_test_questions WHERE category = :category AND is_active = true ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<TestQuestionEntity> findRandomByCategory(@Param("category") String category, @Param("limit") int limit);

    @Query(value = "SELECT * FROM referee_test_questions WHERE is_active = true ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<TestQuestionEntity> findRandomQuestions(@Param("limit") int limit);

    long countByIsActiveTrue();
}
