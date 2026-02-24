package com.pickleball.domain.valueobjects;

import java.math.BigDecimal;
import java.util.Objects;

public class Location {
    private final BigDecimal latitude;
    private final BigDecimal longitude;

    public Location(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Latitude and longitude cannot be null");
        }
        if (latitude.compareTo(new BigDecimal("-90")) < 0 || latitude.compareTo(new BigDecimal("90")) > 0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude.compareTo(new BigDecimal("-180")) < 0 || longitude.compareTo(new BigDecimal("180")) > 0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }

        this.latitude = latitude;
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }

    public double distanceTo(Location other) {
        // Simplified distance calculation (Haversine would be better for production)
        double latDiff = other.latitude.subtract(this.latitude).doubleValue();
        double lonDiff = other.longitude.subtract(this.longitude).doubleValue();
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(latitude, location.latitude) && Objects.equals(longitude, location.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", latitude, longitude);
    }
}