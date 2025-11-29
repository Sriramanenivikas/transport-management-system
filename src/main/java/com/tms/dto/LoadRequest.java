package com.tms.dto;

import com.tms.entity.WeightUnit;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadRequest {

    @NotBlank(message = "Shipper ID is required")
    private String shipperId;

    @NotBlank(message = "Loading city is required")
    private String loadingCity;

    @NotBlank(message = "Unloading city is required")
    private String unloadingCity;

    @NotNull(message = "Loading date is required")
    @FutureOrPresent(message = "Loading date must be in the present or future")
    private LocalDateTime loadingDate;

    @NotBlank(message = "Product type is required")
    private String productType;

    @Positive(message = "Weight must be positive")
    private double weight;

    @NotNull(message = "Weight unit is required")
    private WeightUnit weightUnit;

    @NotBlank(message = "Truck type is required")
    private String truckType;

    @Min(value = 1, message = "Number of trucks must be at least 1")
    private int noOfTrucks;
}
