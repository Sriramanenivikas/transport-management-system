package com.tms.dto;

import com.tms.entity.BidStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResponse {
    private UUID bidId;
    private UUID loadId;
    private UUID transporterId;
    private String transporterCompanyName;
    private double proposedRate;
    private int trucksOffered;
    private BidStatus status;
    private LocalDateTime submittedAt;
    private Double score;
}
