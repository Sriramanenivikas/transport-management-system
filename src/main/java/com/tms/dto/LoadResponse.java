package com.tms.dto;

import java.time.LocalDateTime;
import java.util.List;

public class LoadResponse {

    private Integer loadId;
    private String shipperId;
    private String loadingCity;
    private String unloadingCity;
    private LocalDateTime loadingDate;
    private String productType;
    private Double weight;
    private String weightUnit;
    private String truckType;
    private Integer noOfTrucks;
    private String status;
    private LocalDateTime datePosted;
    private Integer remainingTrucks;
    private List<BidResponse> bids;

    // Getters and Setters
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

    public Integer getRemainingTrucks() { return remainingTrucks; }
    public void setRemainingTrucks(Integer remainingTrucks) { this.remainingTrucks = remainingTrucks; }

    public List<BidResponse> getBids() { return bids; }
    public void setBids(List<BidResponse> bids) { this.bids = bids; }
}

