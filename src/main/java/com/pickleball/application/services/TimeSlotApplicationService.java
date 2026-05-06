package com.pickleball.application.services;

import com.pickleball.application.dtos.TimeSlotDTO;
import com.pickleball.application.usecases.timeslot.GetAvailableSlotsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeSlotApplicationService {

    private final GetAvailableSlotsUseCase getAvailableSlotsUseCase;

    public List<TimeSlotDTO> getAvailableSlots(Long courtId, LocalDate date) {
        List<GetAvailableSlotsUseCase.TimeSlotInfo> slots =
                getAvailableSlotsUseCase.executeAvailableOnly(courtId, date);

        return slots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TimeSlotDTO> getAllSlots(Long courtId, LocalDate date) {
        List<GetAvailableSlotsUseCase.TimeSlotInfo> slots =
                getAvailableSlotsUseCase.execute(courtId, date);

        return slots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TimeSlotDTO convertToDTO(GetAvailableSlotsUseCase.TimeSlotInfo slot) {
        return TimeSlotDTO.builder()
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .priceAmount(slot.getPrice().getAmount())
                .isAvailable(slot.isAvailable())
                .build();
    }
}



