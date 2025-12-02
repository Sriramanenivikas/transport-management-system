package com.tms.dto;

import java.util.List;

public class TransporterResponse {

    private Integer transporterId;
    private String companyName;
    private Double rating;
    private List<TruckInfo> availableTrucks;

    public Integer getTransporterId() { return transporterId; }
    public void setTransporterId(Integer transporterId) { this.transporterId = transporterId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public List<TruckInfo> getAvailableTrucks() { return availableTrucks; }
    public void setAvailableTrucks(List<TruckInfo> availableTrucks) { this.availableTrucks = availableTrucks; }

    public static class TruckInfo {
        private String truckType;
        private Integer count;

        public TruckInfo() {}

        public TruckInfo(String truckType, Integer count) {
            this.truckType = truckType;
            this.count = count;
        }

        public String getTruckType() { return truckType; }
        public void setTruckType(String truckType) { this.truckType = truckType; }

        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }
}

