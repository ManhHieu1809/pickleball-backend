package com.pickleball.application.services;

import com.pickleball.application.dtos.AdminUserDTO;
import com.pickleball.application.dtos.AdminUserStatsDTO;
import com.pickleball.infrastructure.persistence.entities.PlayerEntity;
import com.pickleball.infrastructure.persistence.entities.RefereeEntity;
import com.pickleball.infrastructure.persistence.entities.UserEntity;
import com.pickleball.infrastructure.persistence.entities.VenueOwnerEntity;
import com.pickleball.infrastructure.persistence.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserManagementService {

    private final UserJpaRepository userJpaRepository;
    private final PlayerJpaRepository playerJpaRepository;
    private final VenueOwnerJpaRepository venueOwnerJpaRepository;
    private final RefereeJpaRepository refereeJpaRepository;
    private final AdminJpaRepository adminJpaRepository;
    private final VenueJpaRepository venueJpaRepository;

    public Page<AdminUserDTO> getAllUsers(int page, int size, String search, String role) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<UserEntity> userPage;
        if (search != null && !search.trim().isEmpty()) {
            userPage = userJpaRepository.searchByKeyword(search.trim(), pageable);
        } else {
            userPage = userJpaRepository.findAll(pageable);
        }

        // Map to DTOs with role enrichment
        Page<AdminUserDTO> dtoPage = userPage.map(this::mapToAdminUserDTO);

        if (role != null && !role.trim().isEmpty() && !role.equalsIgnoreCase("ALL")) {
            return dtoPage;
        }

        return dtoPage;
    }

    /**
     * Get a single user's full details by ID.
     */
    public AdminUserDTO getUserById(Long userId) {
        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        return mapToAdminUserDTO(user);
    }

    /**
     * Get user statistics for the admin dashboard.
     */
    public AdminUserStatsDTO getUserStats() {
        long totalUsers = userJpaRepository.count();
        long totalPlayers = playerJpaRepository.count();
        long totalOwners = venueOwnerJpaRepository.count();
        long totalReferees = refereeJpaRepository.count();
        long totalAdmins = adminJpaRepository.count();

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        long newUsersToday = userJpaRepository.countByCreatedAtAfter(startOfToday);
        long newUsersThisMonth = userJpaRepository.countByCreatedAtAfter(startOfMonth);

        return AdminUserStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalPlayers(totalPlayers)
                .totalOwners(totalOwners)
                .totalReferees(totalReferees)
                .totalAdmins(totalAdmins)
                .newUsersToday(newUsersToday)
                .newUsersThisMonth(newUsersThisMonth)
                .build();
    }

    /**
     * Map a UserEntity to AdminUserDTO with role-specific enrichment.
     */
    private AdminUserDTO mapToAdminUserDTO(UserEntity user) {
        List<String> roles = new ArrayList<>();

        // Player info
        Integer currentElo = null;
        Integer loyaltyPoints = null;
        String loyaltyTier = null;
        Optional<PlayerEntity> playerOpt = playerJpaRepository.findByUserId(user.getId());
        if (playerOpt.isPresent()) {
            roles.add("PLAYER");
            PlayerEntity player = playerOpt.get();
            currentElo = player.getCurrentElo();
            loyaltyPoints = player.getLoyaltyPoints();
            loyaltyTier = player.getLoyaltyTier() != null ? player.getLoyaltyTier().name() : null;
        }

        // Owner info
        String taxCode = null;
        String bankAccountNumber = null;
        String bankName = null;
        Integer venueCount = null;
        Optional<VenueOwnerEntity> ownerOpt = venueOwnerJpaRepository.findByUserId(user.getId());
        if (ownerOpt.isPresent()) {
            roles.add("VENUE_OWNER");
            VenueOwnerEntity owner = ownerOpt.get();
            taxCode = owner.getTaxCode();
            bankAccountNumber = owner.getBankAccountNumber();
            bankName = owner.getBankName();
            venueCount = (int) venueJpaRepository.countByOwnerId(user.getId());
        }

        // Referee info
        String refereeType = null;
        Boolean testPassed = null;
        Boolean refereeActive = null;
        Optional<RefereeEntity> refereeOpt = refereeJpaRepository.findByUserId(user.getId());
        if (refereeOpt.isPresent()) {
            roles.add("REFEREE");
            RefereeEntity referee = refereeOpt.get();
            refereeType = referee.getRefereeType() != null ? referee.getRefereeType().name() : null;
            testPassed = referee.getTestPassed();
            refereeActive = referee.getIsActive();
        }

        // Admin check
        if (adminJpaRepository.existsByUserId(user.getId())) {
            roles.add("ADMIN");
        }

        // If no specific role found, default to PLAYER
        if (roles.isEmpty()) {
            roles.add("PLAYER");
        }

        return AdminUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .profilePictureUrl(user.getProfilePictureUrl())
                .createdAt(user.getCreatedAt())
                .roles(roles)
                .currentElo(currentElo)
                .loyaltyPoints(loyaltyPoints)
                .loyaltyTier(loyaltyTier)
                .taxCode(taxCode)
                .bankAccountNumber(bankAccountNumber)
                .bankName(bankName)
                .venueCount(venueCount)
                .refereeType(refereeType)
                .testPassed(testPassed)
                .refereeActive(refereeActive)
                .build();
    }
}
