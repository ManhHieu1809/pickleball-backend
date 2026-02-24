package com.pickleball.application.services;

import com.pickleball.application.dtos.BookingDTO;
import com.pickleball.application.dtos.PaymentDTO;
import com.pickleball.application.dtos.VenueStaffDTO;
import com.pickleball.application.dtos.requests.CreateVenueStaffRequest;
import com.pickleball.application.dtos.requests.CreateWalkInBookingRequest;
import com.pickleball.application.dtos.requests.StaffLoginRequest;
import com.pickleball.application.usecases.booking.CreateWalkInBookingUseCase;
import com.pickleball.application.usecases.booking.CreateWalkInBookingUseCase.WalkInBookingResult;
import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.entities.VenueStaff;
import com.pickleball.domain.repositories.CourtRepository;
import com.pickleball.domain.repositories.VenueRepository;
import com.pickleball.domain.repositories.VenueStaffRepository;
import com.pickleball.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VenueStaffApplicationService {

    private final VenueStaffRepository venueStaffRepository;
    private final VenueRepository venueRepository;
    private final CourtRepository courtRepository;
    private final CreateWalkInBookingUseCase createWalkInBookingUseCase;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public VenueStaffDTO createStaff(CreateVenueStaffRequest request, Long ownerId) {
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue không tồn tại"));

        if (!venue.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Chỉ chủ sân mới có quyền tạo nhân viên");
        }

        // Check username uniqueness
        if (venueStaffRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        // Create staff with default permissions
        Set<String> permissions = request.getPermissions() != null
            ? request.getPermissions()
            : getDefaultPermissions();

        VenueStaff staff = VenueStaff.builder()
                .venueId(request.getVenueId())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .permissions(permissions)
                .build();

        VenueStaff savedStaff = venueStaffRepository.save(staff);
        return convertToDTO(savedStaff, venue.getName());
    }

    /**
     * Staff login - returns JWT token
     */
    public StaffLoginResponse login(StaffLoginRequest request) {
        VenueStaff staff = venueStaffRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Username hoặc mật khẩu không đúng"));

        if (!staff.isActive()) {
            throw new IllegalArgumentException("Tài khoản đã bị vô hiệu hóa");
        }

        if (!passwordEncoder.matches(request.getPassword(), staff.getPasswordHash())) {
            throw new IllegalArgumentException("Username hoặc mật khẩu không đúng");
        }

        // Generate JWT token for staff
        String token = jwtService.generateStaffToken(staff.getId(), staff.getUsername(), staff.getVenueId());

        Venue venue = venueRepository.findById(staff.getVenueId()).orElse(null);
        String venueName = venue != null ? venue.getName() : "";

        return new StaffLoginResponse(
                token,
                convertToDTO(staff, venueName)
        );
    }

    public BookingDTO createWalkInBooking(Long staffId, CreateWalkInBookingRequest request) {
        WalkInBookingResult result = createWalkInBookingUseCase.execute(
                staffId,
                request.getCourtId(),
                request.getStartTime(),
                request.getEndTime(),
                request.getCustomerName(),
                request.getCustomerPhone(),
                request.getPaymentMethod(),
                request.getNotes()
        );

        return convertBookingToDTO(result.booking(), result);
    }


    @Transactional(readOnly = true)
    public List<VenueStaffDTO> getStaffByVenue(Long venueId, Long ownerId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue không tồn tại"));

        if (!venue.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Không có quyền xem nhân viên của venue này");
        }

        return venueStaffRepository.findByVenueId(venueId).stream()
                .map(staff -> convertToDTO(staff, venue.getName()))
                .collect(Collectors.toList());
    }

    public VenueStaffDTO deactivateStaff(Long staffId, Long ownerId) {
        VenueStaff staff = venueStaffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));

        Venue venue = venueRepository.findById(staff.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue không tồn tại"));

        if (!venue.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Không có quyền vô hiệu hóa nhân viên này");
        }

        staff.setActive(false);
        VenueStaff saved = venueStaffRepository.save(staff);
        return convertToDTO(saved, venue.getName());
    }

    public VenueStaffDTO activateStaff(Long staffId, Long ownerId) {
        VenueStaff staff = venueStaffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));

        Venue venue = venueRepository.findById(staff.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue không tồn tại"));

        if (!venue.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Không có quyền kích hoạt nhân viên này");
        }

        staff.setActive(true);
        VenueStaff saved = venueStaffRepository.save(staff);
        return convertToDTO(saved, venue.getName());
    }

    public VenueStaffDTO updatePermissions(Long staffId, Long ownerId, Set<String> permissions) {
        VenueStaff staff = venueStaffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));

        Venue venue = venueRepository.findById(staff.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue không tồn tại"));

        if (!venue.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Không có quyền cập nhật quyền của nhân viên này");
        }

        staff.setPermissions(permissions);
        VenueStaff saved = venueStaffRepository.save(staff);
        return convertToDTO(saved, venue.getName());
    }

    private Set<String> getDefaultPermissions() {
        Set<String> defaults = new HashSet<>();
        defaults.add(VenueStaff.PERM_CREATE_BOOKING);
        defaults.add(VenueStaff.PERM_CHECK_IN);
        return defaults;
    }

    private VenueStaffDTO convertToDTO(VenueStaff staff, String venueName) {
        return VenueStaffDTO.builder()
                .id(staff.getId())
                .venueId(staff.getVenueId())
                .venueName(venueName)
                .username(staff.getUsername())
                .fullName(staff.getFullName())
                .isActive(staff.isActive())
                .permissions(staff.getPermissions())
                .createdAt(staff.getCreatedAt())
                .build();
    }

    private BookingDTO convertBookingToDTO(Booking booking, WalkInBookingResult result) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setCourtId(booking.getCourtId());

        // Load court and venue info
        courtRepository.findById(booking.getCourtId()).ifPresent(court -> {
            dto.setCourtName(court.getCourtName());
            dto.setVenueId(court.getVenueId());

            venueRepository.findById(court.getVenueId()).ifPresent(venue -> {
                dto.setVenueName(venue.getName());
            });
        });

        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setBookingType(booking.getBookingType());
        dto.setStatus(booking.getStatus());
        dto.setCreatedByStaffId(booking.getCreatedByStaffId());

        // Customer info from WalkInBookingResult
        dto.setCustomerName(result.customerName());
        dto.setCustomerPhone(result.customerPhone());
        dto.setPaymentMethod(result.paymentMethod());
        dto.setNotes(booking.getNotes());

        // Cost breakdown
        if (booking.getVenueFee() != null) {
            dto.setVenueFee(booking.getVenueFee().getAmount());
        }
        if (booking.getPlatformFee() != null) {
            dto.setPlatformFee(booking.getPlatformFee().getAmount());
        }
        if (booking.getTotalCost() != null) {
            dto.setTotalCost(booking.getTotalCost().getAmount());
        }

        dto.setCreatedAt(booking.getCreatedAt());

        PaymentDTO payment = PaymentDTO.builder()
                .transactionId("WALK_IN_TXN_" + booking.getId() + "_" + System.currentTimeMillis())
                .status("SUCCESS")
                .amount(dto.getTotalCost())
                .currency("VND")
                .message("Walk-in payment recorded")
                .build();
        dto.setPayment(payment);

        return dto;
    }

    // Response record for staff login
    public record StaffLoginResponse(
            String accessToken,
            VenueStaffDTO staff
    ) {}
}
