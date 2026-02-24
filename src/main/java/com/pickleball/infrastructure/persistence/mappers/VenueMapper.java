package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.valueobjects.Location;
import com.pickleball.infrastructure.persistence.entities.VenueEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;

@Component
public class VenueMapper {
    private final ObjectMapper objectMapper;

    public VenueMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public VenueEntity toEntity(Venue domainVenue) {
        if (domainVenue == null) {
            return null;
        }

        VenueEntity entity = new VenueEntity();
        entity.setId(domainVenue.getId());
        entity.setOwnerId(domainVenue.getOwnerId());
        entity.setName(domainVenue.getName());
        entity.setAddress(domainVenue.getAddress());

        if (domainVenue.getLocation() != null) {
            entity.setLatitude(domainVenue.getLocation().getLatitude());
            entity.setLongitude(domainVenue.getLocation().getLongitude());
        }

        entity.setDescription(domainVenue.getDescription());
        entity.setIsActive(domainVenue.isActive());
        entity.setApprovedByAdminId(domainVenue.getApprovedByAdminId());
        entity.setApprovedAt(domainVenue.getApprovedAt());
        entity.setCreatedAt(domainVenue.getCreatedAt());
        entity.setDeactivatedByAdminId(domainVenue.getDeactivatedByAdminId());

        // Convert amenities to JSON
        if (domainVenue.getAmenities() != null && !domainVenue.getAmenities().isEmpty()) {
            try {
                entity.setAmenities(objectMapper.writeValueAsString(domainVenue.getAmenities()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize amenities", e);
            }
        }

        return entity;
    }

    public Venue toDomain(VenueEntity entity) {
        if (entity == null) {
            return null;
        }

        List<String> amenities = new ArrayList<>();
        if (entity.getAmenities() != null && !entity.getAmenities().isEmpty()) {
            try {
                amenities = objectMapper.readValue(
                        entity.getAmenities(),
                        new TypeReference<>() {}
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse amenities", e);
            }
        }

        Venue domainVenue = Venue.builder()
                .id(entity.getId())
                .ownerId(entity.getOwnerId())
                .name(entity.getName())
                .address(entity.getAddress())
                .location(new Location(entity.getLatitude(), entity.getLongitude()))
                .description(entity.getDescription())
                .amenities(amenities)
                .isActive(entity.getIsActive())
                .approvedByAdminId(entity.getApprovedByAdminId())
                .approvedAt(entity.getApprovedAt())
                .createdAt(entity.getCreatedAt())
                .deactivatedByAdminId(entity.getDeactivatedByAdminId())
                .build();

        return domainVenue;
    }
}