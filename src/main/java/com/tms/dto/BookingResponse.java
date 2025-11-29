package com.tms.dto;

import com.tms.entity.BookingStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private UUID bookingId;
    private UUID loadId;
    private UUID bidId;
    private UUID transporterId;
    private String transporterCompanyName;
    private int allocatedTrucks;
    private double finalRate;
    private BookingStatus status;
    private LocalDateTime bookedAt;
}
