package com.pickleball.application.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitTestAnswersRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Answers are required")
    @Size(min = 10, max = 10, message = "Must answer exactly 10 questions")
    private Map<Long, String> answers;
}
