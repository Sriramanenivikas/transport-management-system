package com.tms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transporters", indexes = {
    @Index(name = "idx_transporter_company_name", columnList = "companyName")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transporter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transporterId;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private double rating;

    @OneToMany(mappedBy = "transporter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<TruckCapacity> availableTrucks = new ArrayList<>();

    public void addTruckCapacity(TruckCapacity truckCapacity) {
        availableTrucks.add(truckCapacity);
        truckCapacity.setTransporter(this);
    }

    public void removeTruckCapacity(TruckCapacity truckCapacity) {
        availableTrucks.remove(truckCapacity);
        truckCapacity.setTransporter(null);
    }
}
