package com.pickleball.application.usecases.referee;

import com.pickleball.domain.entities.TestQuestion;
import com.pickleball.domain.enums.QuestionCategory;
import com.pickleball.domain.repositories.TestQuestionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generate a referee AI test with 10 random questions (2 per category).
 */
public class GenerateRefereeTestUseCase {

    private final TestQuestionRepository testQuestionRepository;

    private static final int QUESTIONS_PER_CATEGORY = 2;
    private static final int TOTAL_QUESTIONS = 10;

    public GenerateRefereeTestUseCase(TestQuestionRepository testQuestionRepository) {
        this.testQuestionRepository = testQuestionRepository;
    }

    public List<TestQuestion> execute() {
        long totalAvailable = testQuestionRepository.countActiveQuestions();
        if (totalAvailable < TOTAL_QUESTIONS) {
            throw new IllegalStateException(
                    "Not enough questions in the bank. Need " + TOTAL_QUESTIONS + ", found " + totalAvailable);
        }

        List<TestQuestion> questions = new ArrayList<>();

        // Get 2 questions from each of the 5 categories
        for (QuestionCategory category : QuestionCategory.values()) {
            List<TestQuestion> categoryQuestions =
                    testQuestionRepository.findRandomQuestionsByCategory(category, QUESTIONS_PER_CATEGORY);

            if (categoryQuestions.size() < QUESTIONS_PER_CATEGORY) {
                throw new IllegalStateException(
                        "Not enough questions for category " + category +
                        ". Need " + QUESTIONS_PER_CATEGORY + ", found " + categoryQuestions.size());
            }
            questions.addAll(categoryQuestions);
        }

        // Shuffle the final list so categories are mixed
        Collections.shuffle(questions);
        return questions;
    }
}
