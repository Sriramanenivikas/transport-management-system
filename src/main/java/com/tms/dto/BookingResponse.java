package com.tms.dto;

import java.time.LocalDateTime;

public class BookingResponse {

    private Integer bookingId;
    private Integer loadId;
    private Integer bidId;
    private Integer transporterId;
    private String transporterCompanyName;
    private Integer allocatedTrucks;
    private Double finalRate;
    private String status;
    private LocalDateTime bookedAt;

    // Getters and Setters
    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public Integer getLoadId() { return loadId; }
    public void setLoadId(Integer loadId) { this.loadId = loadId; }

    public Integer getBidId() { return bidId; }
    public void setBidId(Integer bidId) { this.bidId = bidId; }

    public Integer getTransporterId() { return transporterId; }
    public void setTransporterId(Integer transporterId) { this.transporterId = transporterId; }

    public String getTransporterCompanyName() { return transporterCompanyName; }
    public void setTransporterCompanyName(String transporterCompanyName) { this.transporterCompanyName = transporterCompanyName; }

    public Integer getAllocatedTrucks() { return allocatedTrucks; }
    public void setAllocatedTrucks(Integer allocatedTrucks) { this.allocatedTrucks = allocatedTrucks; }

    public Double getFinalRate() { return finalRate; }
    public void setFinalRate(Double finalRate) { this.finalRate = finalRate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getBookedAt() { return bookedAt; }
    public void setBookedAt(LocalDateTime bookedAt) { this.bookedAt = bookedAt; }
}

