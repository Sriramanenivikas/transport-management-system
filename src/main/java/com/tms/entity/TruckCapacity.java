package com.tms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "truck_capacity")
public class TruckCapacity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "truck_type", nullable = false)
    private String truckType;

    @Column(nullable = false)
    private Integer count;

    @Version
    @Column(name = "version")
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transporter_id", nullable = false)
    @JsonIgnore
    private Transporter transporter;

    // Constructors
    public TruckCapacity() {}

    public TruckCapacity(String truckType, Integer count) {
        this.truckType = truckType;
        this.count = count;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTruckType() { return truckType; }
    public void setTruckType(String truckType) { this.truckType = truckType; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Transporter getTransporter() { return transporter; }
    public void setTransporter(Transporter transporter) { this.transporter = transporter; }
}

