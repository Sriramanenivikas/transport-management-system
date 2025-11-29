package com.tms.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidRequest {

    @NotNull(message = "Load ID is required")
    private UUID loadId;

    @NotNull(message = "Transporter ID is required")
    private UUID transporterId;

    @Positive(message = "Proposed rate must be positive")
    private double proposedRate;

    @Min(value = 1, message = "Trucks offered must be at least 1")
    private int trucksOffered;
}
