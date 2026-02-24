package com.pickleball.application.services;

import com.pickleball.application.dtos.CourtDTO;
import com.pickleball.application.dtos.CourtPricingDTO;
import com.pickleball.application.dtos.DetailedCourtDTO;
import com.pickleball.application.dtos.VenueDTO;
import com.pickleball.application.dtos.requests.CreateCourtRequest;
import com.pickleball.application.dtos.requests.CreateVenueRequest;
import com.pickleball.application.usecases.venue.CreateCourtUseCase;
import com.pickleball.application.usecases.venue.CreateVenueUseCase;
import com.pickleball.application.usecases.venue.GetActiveCourtsUseCase;
import com.pickleball.application.usecases.venue.GetCourtByIdUseCase;
import com.pickleball.application.usecases.venue.GetVenueCourtsUseCase;
import com.pickleball.application.usecases.venue.SearchVenuesUseCase;
import com.pickleball.application.usecases.venue.ToggleCourtStatusUseCase;
import com.pickleball.application.usecases.venue.ToggleVenueStatusUseCase;
import com.pickleball.domain.entities.Court;
import com.pickleball.domain.entities.CourtPricing;
import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.entities.VenueOwner;
import com.pickleball.domain.repositories.CourtPricingRepository;
import com.pickleball.domain.repositories.VenueRepository;
import com.pickleball.domain.repositories.VenueOwnerRepository;
import com.pickleball.domain.valueobjects.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueApplicationService {

    private final CreateVenueUseCase createVenueUseCase;
    private final SearchVenuesUseCase searchVenuesUseCase;
    private final CreateCourtUseCase createCourtUseCase;
    private final GetVenueCourtsUseCase getVenueCourtsUseCase;
    private final GetCourtByIdUseCase getCourtByIdUseCase;
    private final ToggleCourtStatusUseCase toggleCourtStatusUseCase;
    private final GetActiveCourtsUseCase getActiveCourtsUseCase;
    private final ToggleVenueStatusUseCase toggleVenueStatusUseCase;
    private final VenueRepository venueRepository;
    private final VenueOwnerRepository venueOwnerRepository;
    private final CourtPricingRepository courtPricingRepository;

    @Transactional
    public VenueDTO createVenue(CreateVenueRequest request) {
        // Check if user is approved as VenueOwner
        VenueOwner venueOwner = venueOwnerRepository.findByUserId(request.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Bạn chưa được phê duyệt làm chủ sân. Vui lòng submit request trước khi tạo venue"));

        // Validate venue owner has complete information
        if (!venueOwner.hasCompleteInformation()) {
            throw new IllegalArgumentException(
                    "Thông tin chủ sân chưa đầy đủ. Vui lòng hoàn tất thông tin thuế và ngân hàng");
        }

        // Create location value object
        Location location = new Location(request.getLatitude(), request.getLongitude());

        // Create venue
        Venue venue = createVenueUseCase.execute(
                request.getOwnerId(),
                request.getName(),
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude()
        );

        // Set additional fields
        venue.setDescription(request.getDescription());

        // Save venue
        Venue savedVenue = venueRepository.save(venue);

        // Convert to DTO
        return convertToDTO(savedVenue);
    }

    @Transactional
    public VenueDTO approveVenue(Long venueId, Long adminId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue not found"));

        // Business logic: approve venue
        venue.approve(adminId);

        // Save
        Venue updatedVenue = venueRepository.save(venue);

        // TODO: Send notification to venue owner

        return convertToDTO(updatedVenue);
    }

    @Transactional
    public VenueDTO rejectVenue(Long venueId, Long adminId, String reason) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue not found"));

        // Check if already approved
        if (venue.isActive() && venue.getApprovedByAdminId() != null) {
            throw new IllegalArgumentException("Cannot reject an already approved venue");
        }

        venue.deactivate(adminId, true);

        // Save
        Venue updatedVenue = venueRepository.save(venue);

        // TODO: Send notification to venue owner with rejection reason
        log.info("Venue {} rejected by admin {}. Reason: {}", venueId, adminId, reason);

        return convertToDTO(updatedVenue);
    }

    public List<VenueDTO> getPendingVenues() {
        return venueRepository.findPendingVenues().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public VenueDTO updateVenue(Long venueId, CreateVenueRequest request, Long ownerId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue not found"));

        // Check ownership
        if (!venue.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Only venue owner can update");
        }

        // Update fields
        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setLocation(new Location(request.getLatitude(), request.getLongitude()));
        venue.setDescription(request.getDescription());

        // Save
        Venue updatedVenue = venueRepository.save(venue);

        return convertToDTO(updatedVenue);
    }

    public List<VenueDTO> getActiveVenues() {
        return searchVenuesUseCase.executeActiveVenues().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<VenueDTO> getNearbyVenues(Double latitude, Double longitude, Double radiusKm) {
        return searchVenuesUseCase.executeNearbyVenues(latitude, longitude, radiusKm).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public VenueDTO getVenueById(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue not found"));

        return convertToDTO(venue);
    }

    public List<VenueDTO> getVenuesByOwner(Long ownerId) {
        return venueRepository.findByOwnerId(ownerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private VenueDTO convertToDTO(Venue venue) {
        VenueDTO dto = new VenueDTO();
        dto.setId(venue.getId());
        dto.setOwnerId(venue.getOwnerId());
        dto.setName(venue.getName());
        dto.setAddress(venue.getAddress());

        if (venue.getLocation() != null) {
            dto.setLatitude(venue.getLocation().getLatitude());
            dto.setLongitude(venue.getLocation().getLongitude());
        }

        dto.setDescription(venue.getDescription());
        dto.setAmenities(venue.getAmenities());
        dto.setIsActive(venue.isActive());
        dto.setApprovedByAdminId(venue.getApprovedByAdminId());
        dto.setApprovedAt(venue.getApprovedAt());
        dto.setCreatedAt(venue.getCreatedAt());
        dto.setDeactivatedByAdminId(venue.getDeactivatedByAdminId());

        return dto;
    }

    // Court-related methods
    @Transactional
    public CourtDTO createCourt(CreateCourtRequest request, Long ownerId) {
        // Verify venue ownership
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue không tồn tại"));

        if (!venue.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Chỉ chủ sân mới có quyền tạo court");
        }

        // Convert pricing requests
        List<CreateCourtUseCase.CourtPricingRequest> pricingRequests = new ArrayList<>();
        if (request.getPricings() != null) {
            pricingRequests = request.getPricings().stream()
                    .map(p -> new CreateCourtUseCase.CourtPricingRequest(
                            p.getStartTime(),
                            p.getEndTime(),
                            p.getPricePerHour(),
                            p.getDayOfWeek()
                    ))
                    .collect(Collectors.toList());
        }

        // Create court
        Court court = createCourtUseCase.execute(
                request.getVenueId(),
                request.getCourtName(),
                pricingRequests
        );

        return convertCourtToDTO(court);
    }

    public List<CourtDTO> getVenueCourts(Long venueId) {
        List<Court> courts = getVenueCourtsUseCase.execute(venueId);
        return courts.stream()
                .map(this::convertCourtToDTO)
                .collect(Collectors.toList());
    }

    private CourtDTO convertCourtToDTO(Court court) {
        List<CourtPricingDTO> pricingDTOs = new ArrayList<>();

        if (court.getPricing() != null) {
            pricingDTOs = court.getPricing().stream()
                    .map(this::convertCourtPricingToDTO)
                    .collect(Collectors.toList());
        } else if (court.getId() != null) {
            // Load pricing from repository if not loaded
            List<CourtPricing> pricings = courtPricingRepository.findByCourtId(court.getId());
            pricingDTOs = pricings.stream()
                    .map(this::convertCourtPricingToDTO)
                    .collect(Collectors.toList());
        }

        return CourtDTO.builder()
                .id(court.getId())
                .venueId(court.getVenueId())
                .courtName(court.getCourtName())
                .isActive(court.isActive())
                .deactivatedByAdminId(court.getDeactivatedByAdminId())
                .pricings(pricingDTOs)
                .build();
    }

    private CourtPricingDTO convertCourtPricingToDTO(CourtPricing pricing) {
        return CourtPricingDTO.builder()
                .id(pricing.getId())
                .courtId(pricing.getCourtId())
                .startTime(pricing.getStartTime())
                .endTime(pricing.getEndTime())
                .pricePerHour(pricing.getPricePerHour().getAmount())
                .dayOfWeek(pricing.getDayOfWeek() != null ? pricing.getDayOfWeek().getValue() : null)
                .build();
    }

    @Transactional
    public CourtDTO activateCourt(Long courtId, Long requesterId, boolean isAdmin) {
        Court court = toggleCourtStatusUseCase.execute(courtId, requesterId, true, isAdmin);
        return convertCourtToDTO(court);
    }

    @Transactional
    public CourtDTO deactivateCourt(Long courtId, Long requesterId, boolean isAdmin) {
        Court court = toggleCourtStatusUseCase.execute(courtId, requesterId, false, isAdmin);
        return convertCourtToDTO(court);
    }

    public CourtDTO getCourtById(Long courtId) {
        Court court = getCourtByIdUseCase.execute(courtId);
        return convertCourtToDTO(court);
    }

    /**
     * Lấy thông tin chi tiết của court bao gồm:
     * - Thông tin venue mà court thuộc về
     * - Danh sách giá theo từng khung giờ
     * - Độ dài mỗi ca chơi (slot duration)
     */
    public DetailedCourtDTO getDetailedCourtById(Long courtId) {
        // Lấy thông tin court
        Court court = getCourtByIdUseCase.execute(courtId);

        // Lấy thông tin venue
        Venue venue = venueRepository.findById(court.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue không tồn tại"));

        // Lấy danh sách pricing
        List<CourtPricing> pricings = courtPricingRepository.findByCourtId(courtId);

        // Convert sang DTO
        return convertToDetailedCourtDTO(court, venue, pricings);
    }

    private DetailedCourtDTO convertToDetailedCourtDTO(Court court, Venue venue, List<CourtPricing> pricings) {
        // Convert venue info
        VenueDTO venueDTO = convertToDTO(venue);

        // Convert pricing list
        List<CourtPricingDTO> pricingDTOs = pricings.stream()
                .map(this::convertCourtPricingToDTO)
                .collect(Collectors.toList());

        // Slot duration: mặc định 60 phút (1 tiếng) cho mỗi ca
        // Có thể cấu hình theo venue trong tương lai
        Integer slotDurationMinutes = 60;

        return DetailedCourtDTO.builder()
                .id(court.getId())
                .venueId(court.getVenueId())
                .courtName(court.getCourtName())
                .isActive(court.isActive())
                .deactivatedByAdminId(court.getDeactivatedByAdminId())
                .venueInfo(venueDTO)
                .pricings(pricingDTOs)
                .slotDurationMinutes(slotDurationMinutes)
                .build();
    }

    public List<CourtDTO> getActiveCourts(Long venueId) {
        List<Court> courts = getActiveCourtsUseCase.execute(venueId);
        return courts.stream()
                .map(this::convertCourtToDTO)
                .collect(Collectors.toList());
    }

    public List<CourtDTO> getAllActiveCourts() {
        List<Court> courts = getActiveCourtsUseCase.executeAll();
        return courts.stream()
                .map(this::convertCourtToDTO)
                .collect(Collectors.toList());
    }

    // Venue status management
    @Transactional
    public VenueDTO activateVenue(Long venueId, Long requesterId, boolean isAdmin) {
        Venue venue = toggleVenueStatusUseCase.execute(venueId, requesterId, true, isAdmin);
        return convertToDTO(venue);
    }

    @Transactional
    public VenueDTO deactivateVenue(Long venueId, Long requesterId, boolean isAdmin) {
        Venue venue = toggleVenueStatusUseCase.execute(venueId, requesterId, false, isAdmin);
        return convertToDTO(venue);
    }
}