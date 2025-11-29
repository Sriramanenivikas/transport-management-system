package com.tms.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransporterResponse {
    private UUID transporterId;
    private String companyName;
    private double rating;
    private List<TruckCapacityDto> availableTrucks;
}
