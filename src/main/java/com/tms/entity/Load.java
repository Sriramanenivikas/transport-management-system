package com.tms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "loads")
public class Load {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "load_id")
    private Integer loadId;

    @Column(name = "shipper_id", nullable = false)
    private String shipperId;

    @Column(name = "loading_city", nullable = false)
    private String loadingCity;

    @Column(name = "unloading_city", nullable = false)
    private String unloadingCity;

    @Column(name = "loading_date", nullable = false)
    private LocalDateTime loadingDate;

    @Column(name = "product_type", nullable = false)
    private String productType;

    @Column(nullable = false)
    private Double weight;

    @Column(name = "weight_unit", nullable = false)
    private String weightUnit;

    @Column(name = "truck_type", nullable = false)
    private String truckType;

    @Column(name = "no_of_trucks", nullable = false)
    private Integer noOfTrucks;

    @Column(nullable = false)
    private String status = "POSTED";

    @Column(name = "date_posted")
    private LocalDateTime datePosted = LocalDateTime.now();

    @Version
    private Integer version;

    public Load() {}

    public Integer getLoadId() { return loadId; }
    public void setLoadId(Integer loadId) { this.loadId = loadId; }

    public String getShipperId() { return shipperId; }
    public void setShipperId(String shipperId) { this.shipperId = shipperId; }

    public String getLoadingCity() { return loadingCity; }
    public void setLoadingCity(String loadingCity) { this.loadingCity = loadingCity; }

    public String getUnloadingCity() { return unloadingCity; }
    public void setUnloadingCity(String unloadingCity) { this.unloadingCity = unloadingCity; }

    public LocalDateTime getLoadingDate() { return loadingDate; }
    public void setLoadingDate(LocalDateTime loadingDate) { this.loadingDate = loadingDate; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public String getWeightUnit() { return weightUnit; }
    public void setWeightUnit(String weightUnit) { this.weightUnit = weightUnit; }

    public String getTruckType() { return truckType; }
    public void setTruckType(String truckType) { this.truckType = truckType; }

    public Integer getNoOfTrucks() { return noOfTrucks; }
    public void setNoOfTrucks(Integer noOfTrucks) { this.noOfTrucks = noOfTrucks; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDatePosted() { return datePosted; }
    public void setDatePosted(LocalDateTime datePosted) { this.datePosted = datePosted; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}

