package com.tms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bids", indexes = {
    @Index(name = "idx_bid_load_id", columnList = "load_id"),
    @Index(name = "idx_bid_transporter_id", columnList = "transporter_id"),
    @Index(name = "idx_bid_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID bidId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "load_id", nullable = false)
    private Load load;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transporter_id", nullable = false)
    private Transporter transporter;

    @Column(nullable = false)
    private double proposedRate;

    @Column(nullable = false)
    private int trucksOffered;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BidStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;
}
