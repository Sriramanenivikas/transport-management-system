package com.tms.service;

import com.tms.dto.*;
import com.tms.entity.*;
import com.tms.exception.*;
import com.tms.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoadService {

    private final LoadRepository loadRepository;
    private final BidRepository bidRepository;
    private final BookingRepository bookingRepository;
    private final TransporterRepository transporterRepository;

    public LoadService(LoadRepository loadRepository, BidRepository bidRepository,
                      BookingRepository bookingRepository, TransporterRepository transporterRepository) {
        this.loadRepository = loadRepository;
        this.bidRepository = bidRepository;
        this.bookingRepository = bookingRepository;
        this.transporterRepository = transporterRepository;
    }

    @Transactional
    public LoadResponse createLoad(LoadRequest request) {
        Load load = new Load();
        load.setShipperId(request.getShipperId());
        load.setLoadingCity(request.getLoadingCity());
        load.setUnloadingCity(request.getUnloadingCity());
        load.setLoadingDate(request.getLoadingDate());
        load.setProductType(request.getProductType());
        load.setWeight(request.getWeight());
        load.setWeightUnit(request.getWeightUnit());
        load.setTruckType(request.getTruckType());
        load.setNoOfTrucks(request.getNoOfTrucks());
        load.setStatus("POSTED");

        load = loadRepository.save(load);
        return toLoadResponse(load);
    }

    public Page<LoadResponse> getLoads(String shipperId, String status, Pageable pageable) {
        Page<Load> loads;

        if (shipperId != null && status != null) {
            loads = loadRepository.findByShipperIdAndStatus(shipperId, status, pageable);
        } else if (shipperId != null) {
            loads = loadRepository.findByShipperId(shipperId, pageable);
        } else if (status != null) {
            loads = loadRepository.findByStatus(status, pageable);
        } else {
            loads = loadRepository.findAll(pageable);
        }

        return loads.map(this::toLoadResponse);
    }

    public LoadResponse getLoadById(Integer loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with ID: " + loadId));

        LoadResponse response = toLoadResponse(load);

        // Get active bids
        List<Bid> bids = bidRepository.findByLoadIdAndStatus(loadId, "PENDING");
        response.setBids(bids.stream().map(this::toBidResponse).collect(Collectors.toList()));

        return response;
    }

    @Transactional
    public LoadResponse cancelLoad(Integer loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with ID: " + loadId));

        if ("BOOKED".equals(load.getStatus())) {
            throw new InvalidStatusTransitionException("Cannot cancel a load that is already booked");
        }

        load.setStatus("CANCELLED");
        load = loadRepository.save(load);

        return toLoadResponse(load);
    }

    public List<BidResponse> getBestBids(Integer loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with ID: " + loadId));

        List<Bid> bids = bidRepository.findByLoadIdAndStatus(loadId, "PENDING");

        return bids.stream()
                .map(bid -> {
                    BidResponse response = toBidResponse(bid);
                    // Calculate score: (1 / proposedRate) * 0.7 + (rating / 5) * 0.3
                    Transporter transporter = transporterRepository.findById(bid.getTransporterId()).orElse(null);
                    if (transporter != null) {
                        double rateScore = (1.0 / bid.getProposedRate()) * 0.7;
                        double ratingScore = (transporter.getRating() / 5.0) * 0.3;
                        response.setScore(rateScore + ratingScore);
                        response.setTransporterCompanyName(transporter.getCompanyName());
                    }
                    return response;
                })
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());
    }

    private LoadResponse toLoadResponse(Load load) {
        LoadResponse response = new LoadResponse();
        response.setLoadId(load.getLoadId());
        response.setShipperId(load.getShipperId());
        response.setLoadingCity(load.getLoadingCity());
        response.setUnloadingCity(load.getUnloadingCity());
        response.setLoadingDate(load.getLoadingDate());
        response.setProductType(load.getProductType());
        response.setWeight(load.getWeight());
        response.setWeightUnit(load.getWeightUnit());
        response.setTruckType(load.getTruckType());
        response.setNoOfTrucks(load.getNoOfTrucks());
        response.setStatus(load.getStatus());
        response.setDatePosted(load.getDatePosted());

        // Calculate remaining trucks
        Integer allocated = bookingRepository.getTotalAllocatedTrucks(load.getLoadId());
        response.setRemainingTrucks(load.getNoOfTrucks() - (allocated != null ? allocated : 0));

        return response;
    }

    private BidResponse toBidResponse(Bid bid) {
        BidResponse response = new BidResponse();
        response.setBidId(bid.getBidId());
        response.setLoadId(bid.getLoadId());
        response.setTransporterId(bid.getTransporterId());
        response.setProposedRate(bid.getProposedRate());
        response.setTrucksOffered(bid.getTrucksOffered());
        response.setStatus(bid.getStatus());
        response.setSubmittedAt(bid.getSubmittedAt());

        // Get transporter name
        transporterRepository.findById(bid.getTransporterId()).ifPresent(t ->
            response.setTransporterCompanyName(t.getCompanyName())
        );

        return response;
    }
}

