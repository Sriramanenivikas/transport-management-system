package com.tms.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public class BookingRequest {

    @NotNull(message = "Bid ID is required")
    private Integer bidId;

    @NotNull(message = "Allocated trucks is required")
    @Min(value = 1, message = "At least 1 truck must be allocated")
    private Integer allocatedTrucks;

    // Getters and Setters
    public Integer getBidId() { return bidId; }
    public void setBidId(Integer bidId) { this.bidId = bidId; }

    public Integer getAllocatedTrucks() { return allocatedTrucks; }
    public void setAllocatedTrucks(Integer allocatedTrucks) { this.allocatedTrucks = allocatedTrucks; }
}

