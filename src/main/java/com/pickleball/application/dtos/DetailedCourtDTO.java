package com.pickleball.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailedCourtDTO {
    private Long id;
    private Long venueId;
    private String courtName;
    private Boolean isActive;
    private Long deactivatedByAdminId;

    // Thông tin venue mà court này thuộc về
    private VenueDTO venueInfo;

    // Danh sách giá theo từng khung giờ
    private List<CourtPricingDTO> pricings;

    // Độ dài mỗi ca chơi (tính bằng phút)
    // Ví dụ: 60 phút = 1 tiếng, 90 phút = 1.5 tiếng
    private Integer slotDurationMinutes;
}

