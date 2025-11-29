package com.tms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loads", indexes = {
    @Index(name = "idx_load_shipper_id", columnList = "shipperId"),
    @Index(name = "idx_load_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Load {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID loadId;

    @Column(nullable = false)
    private String shipperId;

    @Column(nullable = false)
    private String loadingCity;

    @Column(nullable = false)
    private String unloadingCity;

    @Column(nullable = false)
    private LocalDateTime loadingDate;

    @Column(nullable = false)
    private String productType;

    @Column(nullable = false)
    private double weight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WeightUnit weightUnit;

    @Column(nullable = false)
    private String truckType;

    @Column(nullable = false)
    private int noOfTrucks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoadStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime datePosted;

    @Version
    private Long version;
}
