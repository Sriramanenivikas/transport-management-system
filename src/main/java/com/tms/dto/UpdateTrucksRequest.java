package com.tms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTrucksRequest {

    @NotEmpty(message = "Available trucks list is required")
    @Valid
    private List<TruckCapacityDto> availableTrucks;
}
