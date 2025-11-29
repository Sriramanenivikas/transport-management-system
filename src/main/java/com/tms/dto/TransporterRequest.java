package com.tms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransporterRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private double rating;

    @NotEmpty(message = "Available trucks list is required")
    @Valid
    private List<TruckCapacityDto> availableTrucks;
}
