package com.tms.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class TransporterRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating must be at most 5.0")
    private Double rating;

    @NotNull(message = "Available trucks is required")
    @Size(min = 1, message = "At least one truck type required")
    private List<TruckCapacityDTO> availableTrucks;

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public List<TruckCapacityDTO> getAvailableTrucks() { return availableTrucks; }
    public void setAvailableTrucks(List<TruckCapacityDTO> availableTrucks) { this.availableTrucks = availableTrucks; }

    public static class TruckCapacityDTO {
        @NotBlank(message = "Truck type is required")
        private String truckType;

        @NotNull(message = "Count is required")
        @Min(value = 0, message = "Count cannot be negative")
        private Integer count;

        public String getTruckType() { return truckType; }
        public void setTruckType(String truckType) { this.truckType = truckType; }

        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }
}

