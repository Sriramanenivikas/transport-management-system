package com.tms.dto;

import java.time.LocalDateTime;

public class BidResponse {

    private Integer bidId;
    private Integer loadId;
    private Integer transporterId;
    private String transporterCompanyName;
    private Double proposedRate;
    private Integer trucksOffered;
    private String status;
    private LocalDateTime submittedAt;
    private Double score;

    // Getters and Setters
    public Integer getBidId() { return bidId; }
    public void setBidId(Integer bidId) { this.bidId = bidId; }

    public Integer getLoadId() { return loadId; }
    public void setLoadId(Integer loadId) { this.loadId = loadId; }

    public Integer getTransporterId() { return transporterId; }
    public void setTransporterId(Integer transporterId) { this.transporterId = transporterId; }

    public String getTransporterCompanyName() { return transporterCompanyName; }
    public void setTransporterCompanyName(String transporterCompanyName) { this.transporterCompanyName = transporterCompanyName; }

    public Double getProposedRate() { return proposedRate; }
    public void setProposedRate(Double proposedRate) { this.proposedRate = proposedRate; }

    public Integer getTrucksOffered() { return trucksOffered; }
    public void setTrucksOffered(Integer trucksOffered) { this.trucksOffered = trucksOffered; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
}

