package com.pickleball.application.dtos.requests;
import lombok.Data;
import java.time.LocalTime;
@Data
public class BlockCourtRequest {
    private String reason;
    private String startTime;
    private String endTime;
    private String repeatType;
    private Long ownerId;
}