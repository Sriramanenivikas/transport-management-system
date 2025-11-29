package com.tms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "truck_capacities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckCapacity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String truckType;

    @Column(nullable = false)
    private int count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transporter_id", nullable = false)
    @JsonIgnore
    private Transporter transporter;
}
