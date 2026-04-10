package com.pickleball.application.services;

import com.pickleball.application.dtos.*;
import com.pickleball.application.dtos.requests.*;
import com.pickleball.application.usecases.booking.SubmitMatchResultUseCase;
import com.pickleball.application.usecases.booking.ResolveDisputeUseCase;
import com.pickleball.application.usecases.booking.SubmitDisputeUseCase;
import com.pickleball.application.usecases.referee.*;
import com.pickleball.domain.entities.*;
import com.pickleball.domain.enums.DisputeDecision;
import com.pickleball.domain.repositories.MatchDisputeRepository;
import com.pickleball.domain.repositories.RefereeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefereeApplicationService {

    private final GenerateRefereeTestUseCase generateRefereeTestUseCase;
    private final SubmitRefereeTestUseCase submitRefereeTestUseCase;
    private final GetRefereeTestHistoryUseCase getRefereeTestHistoryUseCase;
    private final ApproveRefereeRequestUseCase approveRefereeRequestUseCase;
    private final RejectRefereeRequestUseCase rejectRefereeRequestUseCase;
    private final SubmitMatchResultUseCase submitMatchResultUseCase;
    private final SubmitDisputeUseCase submitDisputeUseCase;
    private final SubmitRefereeEvidenceUseCase submitRefereeEvidenceUseCase;
    private final ResolveDisputeUseCase resolveDisputeUseCase;
    private final RefereeRepository refereeRepository;
    private final MatchDisputeRepository matchDisputeRepository;

    public List<TestQuestionDTO> generateTest() {
        List<TestQuestion> questions = generateRefereeTestUseCase.execute();
        return questions.stream()
                .map(this::toQuestionDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public RefereeTestResultDTO submitTest(SubmitTestAnswersRequest request) {
        TestAttempt attempt = submitRefereeTestUseCase.execute(request.getUserId(), request.getAnswers());
        return toTestResultDTO(attempt);
    }

    public List<RefereeTestResultDTO> getTestHistory(Long userId) {
        List<TestAttempt> attempts = getRefereeTestHistoryUseCase.execute(userId);
        return attempts.stream()
                .map(this::toTestResultDTO)
                .collect(Collectors.toList());
    }

    public RefereeDTO getRefereeProfile(Long userId) {
        Referee referee = refereeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Referee not found with userId: " + userId));
        return toRefereeDTO(referee);
    }

    @Transactional
    public RoleRequestDTO approveRefereeRequest(Long requestId, Long adminId) {
        RoleRequest request = approveRefereeRequestUseCase.execute(requestId, adminId);
        return toRoleRequestDTO(request);
    }

    @Transactional
    public RoleRequestDTO rejectRefereeRequest(Long requestId, Long adminId, String notes) {
        RoleRequest request = rejectRefereeRequestUseCase.execute(requestId, adminId, notes);
        return toRoleRequestDTO(request);
    }

    // ==================== Match Result ====================

    @Transactional
    public void submitMatchResult(Long matchId, SubmitMatchResultRequest request) {
        submitMatchResultUseCase.executeByMatchId(
                matchId,
                request.getRefereeUserId(),
                request.getTeamAScore(),
                request.getTeamBScore());
    }

    // ==================== Disputes ====================

    @Transactional
    public MatchDisputeDTO submitDispute(SubmitDisputeRequest request) {
        MatchDispute dispute = submitDisputeUseCase.execute(
                request.getRankedMatchId(),
                request.getReportingPlayerId(),
                request.getReason(),
                request.getEvidence());
        return toDisputeDTO(dispute);
    }

    @Transactional
    public MatchDisputeDTO submitRefereeEvidence(Long disputeId, SubmitRefereeEvidenceRequest request) {
        MatchDispute dispute = submitRefereeEvidenceUseCase.execute(
                disputeId,
                request.getRefereeUserId(),
                request.getEvidenceUrl(),
                request.getResponse());
        return toDisputeDTO(dispute);
    }

    @Transactional
    public MatchDisputeDTO resolveDispute(Long disputeId, ResolveDisputeRequest request) {
        MatchDispute dispute = resolveDisputeUseCase.execute(
                disputeId,
                request.getAdminId(),
                request.getDecisionType(),
                request.getDecision() // Admin decision text
        );
        return toMatchDisputeDTO(dispute);
    }

    public List<MatchDisputeDTO> getAllDisputes() {
        return matchDisputeRepository.findAll().stream()
                .map(this::toMatchDisputeDTO)
                .collect(Collectors.toList());
    }

    public MatchDisputeDTO getDisputeById(Long disputeId) {
        MatchDispute dispute = matchDisputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found with id: " + disputeId));
        return toDisputeDTO(dispute);
    }

    // ==================== Mappers ====================

    private TestQuestionDTO toQuestionDTO(TestQuestion question) {
        return TestQuestionDTO.builder()
                .id(question.getId())
                .category(question.getCategory())
                .questionText(question.getQuestionText())
                .optionA(question.getOptionA())
                .optionB(question.getOptionB())
                .optionC(question.getOptionC())
                .optionD(question.getOptionD())
                // NOTE: correctAnswer NOT included
                .build();
    }

    private RefereeTestResultDTO toTestResultDTO(TestAttempt attempt) {
        String message = attempt.hasPassed()
                ? "Congratulations! You passed the referee test. Your request is pending admin approval."
                : "You did not pass. Score: " + attempt.getScore() + "/" + attempt.getTotalQuestions()
                  + ". You need at least 9/10 to pass. Please try again.";

        return RefereeTestResultDTO.builder()
                .attemptId(attempt.getId())
                .userId(attempt.getUserId())
                .score(attempt.getScore())
                .totalQuestions(attempt.getTotalQuestions())
                .passed(attempt.getPassed())
                .attemptedAt(attempt.getAttemptedAt())
                .message(message)
                .build();
    }

    private RefereeDTO toRefereeDTO(Referee referee) {
        return RefereeDTO.builder()
                .userId(referee.getUserId())
                .testPassed(referee.getTestPassed())
                .testScore(referee.getTestScore())
                .refereeType(referee.getRefereeType())
                .worksAtVenueId(referee.getWorksAtVenueId())
                .isActive(referee.getIsActive())
                .trustScore(referee.getTrustScore())
                .totalMatchesRefereed(referee.getTotalMatchesRefereed())
                .approvedAt(referee.getApprovedAt())
                .build();
    }

    private MatchDisputeDTO toDisputeDTO(MatchDispute dispute) {
        return MatchDisputeDTO.builder()
                .id(dispute.getId())
                .rankedMatchId(dispute.getRankedMatchId())
                .reportingPlayerId(dispute.getReportingPlayerId())
                .reason(dispute.getReason())
                .evidence(dispute.getEvidence())
                .refereeEvidenceUrl(dispute.getRefereeEvidenceUrl())
                .refereeResponse(dispute.getRefereeResponse())
                .status(dispute.getStatus())
                .evidenceDeadline(dispute.getEvidenceDeadline())
                .resolvedByAdminId(dispute.getResolvedByAdminId())
                .adminDecision(dispute.getAdminDecision())
                .decisionType(dispute.getDecisionType())
                .resolvedAt(dispute.getResolvedAt())
                .build();
    }

    private MatchDisputeDTO toMatchDisputeDTO(MatchDispute dispute) {
        return MatchDisputeDTO.builder()
                .id(dispute.getId())
                .rankedMatchId(dispute.getRankedMatchId())
                .reportingPlayerId(dispute.getReportingPlayerId())
                .reason(dispute.getReason())
                .evidence(dispute.getEvidence())
                .refereeEvidenceUrl(dispute.getRefereeEvidenceUrl())
                .refereeResponse(dispute.getRefereeResponse())
                .status(dispute.getStatus())
                .evidenceDeadline(dispute.getEvidenceDeadline())
                .resolvedByAdminId(dispute.getResolvedByAdminId())
                .adminDecision(dispute.getAdminDecision())
                .decisionType(dispute.getDecisionType())
                .resolvedAt(dispute.getResolvedAt())
                .build();
    }

    private RoleRequestDTO toRoleRequestDTO(RoleRequest request) {
        return RoleRequestDTO.builder()
                .id(request.getId())
                .userId(request.getUserId())
                .requestType(request.getRequestType())
                .venueId(request.getVenueId())
                .legalInfo(request.getLegalInfo())
                .testScore(request.getTestScore())
                .status(request.getStatus())
                .submittedAt(request.getSubmittedAt())
                .processedByAdminId(request.getProcessedByAdminId())
                .processedAt(request.getProcessedAt())
                .notes(request.getNotes())
                .build();
    }
}
