package com.tms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Integer bookingId;

    @Column(name = "load_id", nullable = false)
    private Integer loadId;

    @Column(name = "bid_id", nullable = false)
    private Integer bidId;

    @Column(name = "transporter_id", nullable = false)
    private Integer transporterId;

    @Column(name = "allocated_trucks", nullable = false)
    private Integer allocatedTrucks;

    @Column(name = "final_rate", nullable = false)
    private Double finalRate;

    @Column(nullable = false)
    private String status = "CONFIRMED";

    @Column(name = "booked_at")
    private LocalDateTime bookedAt = LocalDateTime.now();

    public Booking() {}

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public Integer getLoadId() { return loadId; }
    public void setLoadId(Integer loadId) { this.loadId = loadId; }

    public Integer getBidId() { return bidId; }
    public void setBidId(Integer bidId) { this.bidId = bidId; }

    public Integer getTransporterId() { return transporterId; }
    public void setTransporterId(Integer transporterId) { this.transporterId = transporterId; }

    public Integer getAllocatedTrucks() { return allocatedTrucks; }
    public void setAllocatedTrucks(Integer allocatedTrucks) { this.allocatedTrucks = allocatedTrucks; }

    public Double getFinalRate() { return finalRate; }
    public void setFinalRate(Double finalRate) { this.finalRate = finalRate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getBookedAt() { return bookedAt; }
    public void setBookedAt(LocalDateTime bookedAt) { this.bookedAt = bookedAt; }
}

