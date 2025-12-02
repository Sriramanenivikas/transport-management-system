package com.tms.dto;

import jakarta.validation.constraints.*;

public class BidRequest {

    @NotNull(message = "Load ID is required")
    private Integer loadId;

    @NotNull(message = "Transporter ID is required")
    private Integer transporterId;

    @NotNull(message = "Proposed rate is required")
    @Positive(message = "Proposed rate must be positive")
    private Double proposedRate;

    @NotNull(message = "Trucks offered is required")
    @Min(value = 1, message = "At least 1 truck must be offered")
    private Integer trucksOffered;

    // Getters and Setters
    public Integer getLoadId() { return loadId; }
    public void setLoadId(Integer loadId) { this.loadId = loadId; }

    public Integer getTransporterId() { return transporterId; }
    public void setTransporterId(Integer transporterId) { this.transporterId = transporterId; }

    public Double getProposedRate() { return proposedRate; }
    public void setProposedRate(Double proposedRate) { this.proposedRate = proposedRate; }

    public Integer getTrucksOffered() { return trucksOffered; }
    public void setTrucksOffered(Integer trucksOffered) { this.trucksOffered = trucksOffered; }
}

