package com.tms.service;

import com.tms.dto.BidRequest;
import com.tms.dto.BidResponse;
import com.tms.entity.Bid;
import com.tms.entity.Load;
import com.tms.entity.Transporter;
import com.tms.entity.TruckCapacity;
import com.tms.exception.*;
import com.tms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BidService {

    private final BidRepository bidRepository;
    private final LoadRepository loadRepository;
    private final TransporterRepository transporterRepository;
    private final TruckCapacityRepository truckCapacityRepository;

    public BidService(BidRepository bidRepository, LoadRepository loadRepository,
                     TransporterRepository transporterRepository, TruckCapacityRepository truckCapacityRepository) {
        this.bidRepository = bidRepository;
        this.loadRepository = loadRepository;
        this.transporterRepository = transporterRepository;
        this.truckCapacityRepository = truckCapacityRepository;
    }

    @Transactional
    public BidResponse createBid(BidRequest request) {
        Load load = loadRepository.findById(request.getLoadId())
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with ID: " + request.getLoadId()));

        if ("CANCELLED".equals(load.getStatus()) || "BOOKED".equals(load.getStatus())) {
            throw new InvalidStatusTransitionException("Cannot bid on a load with status: " + load.getStatus());
        }

        Transporter transporter = transporterRepository.findById(request.getTransporterId())
                .orElseThrow(() -> new ResourceNotFoundException("Transporter not found with ID: " + request.getTransporterId()));

        TruckCapacity capacity = truckCapacityRepository
                .findByTransporterTransporterIdAndTruckType(request.getTransporterId(), load.getTruckType())
                .orElseThrow(() -> new InsufficientCapacityException(
                        "Transporter does not have " + load.getTruckType() + " truck type"));

        if (capacity.getCount() < request.getTrucksOffered()) {
            throw new InsufficientCapacityException(
                    String.format("Transporter only has %d trucks available, but bid offers %d",
                            capacity.getCount(), request.getTrucksOffered()));
        }

        Bid bid = new Bid();
        bid.setLoadId(request.getLoadId());
        bid.setTransporterId(request.getTransporterId());
        bid.setProposedRate(request.getProposedRate());
        bid.setTrucksOffered(request.getTrucksOffered());
        bid.setStatus("PENDING");

        bid = bidRepository.save(bid);

        if ("POSTED".equals(load.getStatus())) {
            load.setStatus("OPEN_FOR_BIDS");
            loadRepository.save(load);
        }

        return toBidResponse(bid, transporter);
    }

    public List<BidResponse> getBids(Integer loadId, Integer transporterId, String status) {
        List<Bid> bids;

        if (loadId != null && status != null) {
            bids = bidRepository.findByLoadIdAndStatus(loadId, status);
        } else if (transporterId != null && status != null) {
            bids = bidRepository.findByTransporterIdAndStatus(transporterId, status);
        } else if (loadId != null) {
            bids = bidRepository.findByLoadId(loadId);
        } else if (transporterId != null) {
            bids = bidRepository.findByTransporterId(transporterId);
        } else if (status != null) {
            bids = bidRepository.findByStatus(status);
        } else {
            bids = bidRepository.findAll();
        }

        return bids.stream()
                .map(bid -> {
                    Transporter t = transporterRepository.findById(bid.getTransporterId()).orElse(null);
                    return toBidResponse(bid, t);
                })
                .collect(Collectors.toList());
    }

    public BidResponse getBidById(Integer bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with ID: " + bidId));

        Transporter transporter = transporterRepository.findById(bid.getTransporterId()).orElse(null);
        return toBidResponse(bid, transporter);
    }

    @Transactional
    public BidResponse rejectBid(Integer bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with ID: " + bidId));

        if ("REJECTED".equals(bid.getStatus()) || "ACCEPTED".equals(bid.getStatus())) {
            throw new InvalidStatusTransitionException("Cannot reject bid with status: " + bid.getStatus());
        }

        bid.setStatus("REJECTED");
        bid = bidRepository.save(bid);

        Transporter transporter = transporterRepository.findById(bid.getTransporterId()).orElse(null);
        return toBidResponse(bid, transporter);
    }


    private BidResponse toBidResponse(Bid bid, Transporter transporter) {
        BidResponse response = new BidResponse();
        response.setBidId(bid.getBidId());
        response.setLoadId(bid.getLoadId());
        response.setTransporterId(bid.getTransporterId());
        response.setProposedRate(bid.getProposedRate());
        response.setTrucksOffered(bid.getTrucksOffered());
        response.setStatus(bid.getStatus());
        response.setSubmittedAt(bid.getSubmittedAt());

        if (transporter != null) {
            response.setTransporterCompanyName(transporter.getCompanyName());
        }

        return response;
    }
}

