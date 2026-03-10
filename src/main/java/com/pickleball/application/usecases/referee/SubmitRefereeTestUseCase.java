package com.pickleball.application.usecases.referee;

import com.pickleball.domain.entities.RoleRequest;
import com.pickleball.domain.entities.TestAttempt;
import com.pickleball.domain.entities.TestQuestion;
import com.pickleball.domain.enums.RequestStatus;
import com.pickleball.domain.enums.RequestType;
import com.pickleball.domain.repositories.RefereeRepository;
import com.pickleball.domain.repositories.RoleRequestRepository;
import com.pickleball.domain.repositories.TestAttemptRepository;
import com.pickleball.domain.repositories.TestQuestionRepository;
import com.pickleball.domain.repositories.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Submit answers for a referee AI test.
 * If score >= 9/10, automatically creates a PENDING referee registration request.
 */
public class SubmitRefereeTestUseCase {

    private final TestQuestionRepository testQuestionRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final RoleRequestRepository roleRequestRepository;
    private final RefereeRepository refereeRepository;
    private final UserRepository userRepository;

    public SubmitRefereeTestUseCase(
            TestQuestionRepository testQuestionRepository,
            TestAttemptRepository testAttemptRepository,
            RoleRequestRepository roleRequestRepository,
            RefereeRepository refereeRepository,
            UserRepository userRepository) {
        this.testQuestionRepository = testQuestionRepository;
        this.testAttemptRepository = testAttemptRepository;
        this.roleRequestRepository = roleRequestRepository;
        this.refereeRepository = refereeRepository;
        this.userRepository = userRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public TestAttempt execute(Long userId, Map<Long, String> answers) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Check if already a referee
        if (refereeRepository.existsByUserId(userId)) {
            throw new IllegalStateException("User is already a referee");
        }

        // Check if there's already a pending referee request
        Optional<RoleRequest> existingRequest = roleRequestRepository
                .findByUserIdAndRequestTypeAndStatus(userId, RequestType.PLATFORM_REFEREE, RequestStatus.PENDING);
        if (existingRequest.isPresent()) {
            throw new IllegalStateException("You already have a pending referee request. Please wait for admin approval.");
        }

        // Get all questions that were answered
        List<Long> questionIds = answers.keySet().stream().collect(Collectors.toList());
        if (questionIds.size() != TestAttempt.TOTAL_QUESTIONS) {
            throw new IllegalArgumentException("Must answer exactly " + TestAttempt.TOTAL_QUESTIONS + " questions");
        }

        // Fetch questions and grade
        List<TestQuestion> questions = testQuestionRepository.findActiveQuestions().stream()
                .filter(q -> questionIds.contains(q.getId()))
                .collect(Collectors.toList());

        if (questions.size() != TestAttempt.TOTAL_QUESTIONS) {
            throw new IllegalArgumentException("Some question IDs are invalid");
        }

        int correctCount = 0;
        for (TestQuestion question : questions) {
            String playerAnswer = answers.get(question.getId());
            if (question.isCorrect(playerAnswer)) {
                correctCount++;
            }
        }

        // Build answers JSON string
        StringBuilder answersJson = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<Long, String> entry : answers.entrySet()) {
            if (!first) answersJson.append(",");
            answersJson.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        answersJson.append("}");

        // Save test attempt
        TestAttempt attempt = TestAttempt.builder()
                .userId(userId)
                .totalQuestions(TestAttempt.TOTAL_QUESTIONS)
                .answers(answersJson.toString())
                .build();
        attempt.calculateResult(correctCount);

        TestAttempt savedAttempt = testAttemptRepository.save(attempt);

        // If passed, create registration request automatically
        if (savedAttempt.hasPassed()) {
            RoleRequest roleRequest = RoleRequest.builder()
                    .userId(userId)
                    .requestType(RequestType.PLATFORM_REFEREE)
                    .testScore(new BigDecimal(correctCount))
                    .status(RequestStatus.PENDING)
                    .submittedAt(LocalDateTime.now())
                    .build();
            roleRequestRepository.save(roleRequest);
        }

        return savedAttempt;
    }
}
