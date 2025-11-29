package com.tms.dto;

import com.tms.entity.LoadStatus;
import com.tms.entity.WeightUnit;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadResponse {
    private UUID loadId;
    private String shipperId;
    private String loadingCity;
    private String unloadingCity;
    private LocalDateTime loadingDate;
    private String productType;
    private double weight;
    private WeightUnit weightUnit;
    private String truckType;
    private int noOfTrucks;
    private LoadStatus status;
    private LocalDateTime datePosted;
    private int remainingTrucks;
    private List<BidResponse> activeBids;
}
