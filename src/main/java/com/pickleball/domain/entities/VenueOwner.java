package com.pickleball.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VenueOwner {
    private Long userId;
    private String taxCode;
    private String bankAccountNumber;
    private String bankName;

    public boolean hasCompleteInformation() {
        return taxCode != null && !taxCode.trim().isEmpty() &&
                bankAccountNumber != null && !bankAccountNumber.trim().isEmpty() &&
                bankName != null && !bankName.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VenueOwner that = (VenueOwner) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}

