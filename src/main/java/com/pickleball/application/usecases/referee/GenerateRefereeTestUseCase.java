package com.pickleball.application.usecases.referee;

import com.pickleball.domain.entities.TestQuestion;
import com.pickleball.domain.enums.QuestionCategory;
import com.pickleball.domain.repositories.TestQuestionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        Collections.shuffle(questions);
        return questions;
    }
}
