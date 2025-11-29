package com.tms.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckCapacityDto {

    @NotBlank(message = "Truck type is required")
    private String truckType;

    @Min(value = 0, message = "Count must be non-negative")
    private int count;
}
