package com.tms.service;

import com.tms.dto.*;
import com.tms.entity.*;
import com.tms.exception.*;
import com.tms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final LoadRepository loadRepository;
    private final TransporterRepository transporterRepository;
    private final TransporterService transporterService;
    private final LoadService loadService;

    @Transactional
    public BidResponse submitBid(BidRequest request) {
        Load load = loadRepository.findById(request.getLoadId())
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + request.getLoadId()));

        // Validate load status - cannot bid on CANCELLED or BOOKED loads
        if (load.getStatus() == LoadStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("Cannot bid on a CANCELLED load");
        }
        if (load.getStatus() == LoadStatus.BOOKED) {
            throw new InvalidStatusTransitionException("Cannot bid on a fully BOOKED load");
        }

        Transporter transporter = transporterRepository.findById(request.getTransporterId())
                .orElseThrow(() -> new ResourceNotFoundException("Transporter not found with id: " + request.getTransporterId()));

        // Validate truck type and capacity
        int availableTrucks = transporterService.getAvailableTrucks(request.getTransporterId(), load.getTruckType());
        if (request.getTrucksOffered() > availableTrucks) {
            throw new InsufficientCapacityException(
                    "Transporter does not have enough trucks. Required: " + request.getTrucksOffered() + 
                    ", Available: " + availableTrucks + " of type: " + load.getTruckType());
        }

        // Validate trucks offered doesn't exceed remaining trucks needed
        int remainingTrucks = loadService.getRemainingTrucks(request.getLoadId());
        if (request.getTrucksOffered() > remainingTrucks) {
            throw new InvalidStatusTransitionException(
                    "Trucks offered (" + request.getTrucksOffered() + ") exceeds remaining trucks needed (" + remainingTrucks + ")");
        }

        Bid bid = Bid.builder()
                .load(load)
                .transporter(transporter)
                .proposedRate(request.getProposedRate())
                .trucksOffered(request.getTrucksOffered())
                .status(BidStatus.PENDING)
                .build();

        Bid savedBid = bidRepository.save(bid);

        // Update load status to OPEN_FOR_BIDS if it was POSTED
        loadService.updateLoadStatusOnBid(request.getLoadId());

        return mapToResponse(savedBid);
    }

    public List<BidResponse> getBids(UUID loadId, UUID transporterId, BidStatus status) {
        List<Bid> bids;

        if (loadId != null && transporterId != null && status != null) {
            bids = bidRepository.findByLoadLoadIdAndTransporterTransporterId(loadId, transporterId)
                    .stream()
                    .filter(b -> b.getStatus() == status)
                    .collect(Collectors.toList());
        } else if (loadId != null && transporterId != null) {
            bids = bidRepository.findByLoadLoadIdAndTransporterTransporterId(loadId, transporterId);
        } else if (loadId != null && status != null) {
            bids = bidRepository.findByLoadLoadIdAndStatus(loadId, status);
        } else if (transporterId != null && status != null) {
            bids = bidRepository.findByTransporterTransporterIdAndStatus(transporterId, status);
        } else if (loadId != null) {
            bids = bidRepository.findByLoadLoadId(loadId);
        } else if (transporterId != null) {
            bids = bidRepository.findByTransporterTransporterId(transporterId);
        } else if (status != null) {
            bids = bidRepository.findByStatus(status);
        } else {
            bids = bidRepository.findAll();
        }

        return bids.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BidResponse getBidById(UUID bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));
        return mapToResponse(bid);
    }

    @Transactional
    public BidResponse rejectBid(UUID bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        if (bid.getStatus() != BidStatus.PENDING) {
            throw new InvalidStatusTransitionException("Can only reject PENDING bids. Current status: " + bid.getStatus());
        }

        bid.setStatus(BidStatus.REJECTED);
        Bid savedBid = bidRepository.save(bid);
        return mapToResponse(savedBid);
    }

    @Transactional
    public void acceptBid(UUID bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        if (bid.getStatus() != BidStatus.PENDING) {
            throw new InvalidStatusTransitionException("Can only accept PENDING bids. Current status: " + bid.getStatus());
        }

        bid.setStatus(BidStatus.ACCEPTED);
        bidRepository.save(bid);
    }

    private BidResponse mapToResponse(Bid bid) {
        return BidResponse.builder()
                .bidId(bid.getBidId())
                .loadId(bid.getLoad().getLoadId())
                .transporterId(bid.getTransporter().getTransporterId())
                .transporterCompanyName(bid.getTransporter().getCompanyName())
                .proposedRate(bid.getProposedRate())
                .trucksOffered(bid.getTrucksOffered())
                .status(bid.getStatus())
                .submittedAt(bid.getSubmittedAt())
                .build();
    }
}
