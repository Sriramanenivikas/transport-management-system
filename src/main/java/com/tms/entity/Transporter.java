package com.tms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transporters")
public class Transporter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transporter_id")
    private Integer transporterId;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(nullable = false)
    private Double rating;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Version
    @Column(name = "version")
    private Integer version;

    @OneToMany(mappedBy = "transporter", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<TruckCapacity> availableTrucks = new ArrayList<>();

    public Transporter() {}

    public Integer getTransporterId() { return transporterId; }
    public void setTransporterId(Integer transporterId) { this.transporterId = transporterId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public List<TruckCapacity> getAvailableTrucks() { return availableTrucks; }
    public void setAvailableTrucks(List<TruckCapacity> availableTrucks) {
        this.availableTrucks = availableTrucks;
        for (TruckCapacity truck : availableTrucks) {
            truck.setTransporter(this);
        }
    }
}

