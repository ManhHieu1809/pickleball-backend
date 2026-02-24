package com.pickleball.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickleball.application.dtos.RoleRequestDTO;
import com.pickleball.application.dtos.requests.SubmitVenueOwnerRequest;
import com.pickleball.application.usecases.user.ApproveVenueOwnerRequestUseCase;
import com.pickleball.application.usecases.user.GetPendingRoleRequestsUseCase;
import com.pickleball.application.usecases.user.RejectVenueOwnerRequestUseCase;
import com.pickleball.application.usecases.user.SubmitVenueOwnerRequestUseCase;
import com.pickleball.domain.entities.RoleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminApplicationService {

    private final SubmitVenueOwnerRequestUseCase submitVenueOwnerRequestUseCase;
    private final ApproveVenueOwnerRequestUseCase approveVenueOwnerRequestUseCase;
    private final RejectVenueOwnerRequestUseCase rejectVenueOwnerRequestUseCase;
    private final GetPendingRoleRequestsUseCase getPendingRoleRequestsUseCase;
    private final ObjectMapper objectMapper;

    @Transactional
    public RoleRequestDTO submitVenueOwnerRequest(SubmitVenueOwnerRequest request) {
        Map<String, String> legalInfo = new HashMap<>();
        legalInfo.put("taxCode", request.getTaxCode());
        legalInfo.put("bankAccountNumber", request.getBankAccountNumber());
        legalInfo.put("bankName", request.getBankName());

        String legalInfoJson;
        try {
            legalInfoJson = objectMapper.writeValueAsString(legalInfo);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize legal information", e);
        }

        RoleRequest roleRequest = submitVenueOwnerRequestUseCase.execute(request.getUserId(), legalInfoJson);
        return convertToDTO(roleRequest);
    }

    public List<RoleRequestDTO> getPendingRequests() {
        return getPendingRoleRequestsUseCase.execute().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoleRequestDTO approveRequest(Long requestId, Long adminId) {
        RoleRequest roleRequest = approveVenueOwnerRequestUseCase.execute(requestId, adminId);
        return convertToDTO(roleRequest);
    }

    @Transactional
    public RoleRequestDTO rejectRequest(Long requestId, Long adminId, String notes) {
        RoleRequest roleRequest = rejectVenueOwnerRequestUseCase.execute(requestId, adminId, notes);
        return convertToDTO(roleRequest);
    }

    private RoleRequestDTO convertToDTO(RoleRequest roleRequest) {
        return RoleRequestDTO.builder()
                .id(roleRequest.getId())
                .userId(roleRequest.getUserId())
                .requestType(roleRequest.getRequestType())
                .venueId(roleRequest.getVenueId())
                .legalInfo(roleRequest.getLegalInfo())
                .testScore(roleRequest.getTestScore())
                .status(roleRequest.getStatus())
                .submittedAt(roleRequest.getSubmittedAt())
                .processedByAdminId(roleRequest.getProcessedByAdminId())
                .processedAt(roleRequest.getProcessedAt())
                .notes(roleRequest.getNotes())
                .build();
    }
}

