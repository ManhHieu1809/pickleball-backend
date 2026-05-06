package com.pickleball.application.usecases.referee;

import com.pickleball.domain.entities.TestAttempt;
import com.pickleball.domain.repositories.TestAttemptRepository;

import java.util.List;

public class GetRefereeTestHistoryUseCase {

    private final TestAttemptRepository testAttemptRepository;

    public GetRefereeTestHistoryUseCase(TestAttemptRepository testAttemptRepository) {
        this.testAttemptRepository = testAttemptRepository;
    }

    public List<TestAttempt> execute(Long userId) {
        return testAttemptRepository.findByUserId(userId);
    }
}
