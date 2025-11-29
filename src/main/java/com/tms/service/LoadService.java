package com.tms.service;

import com.tms.dto.*;
import com.tms.entity.*;
import com.tms.exception.*;
import com.tms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoadService {

    private final LoadRepository loadRepository;
    private final BidRepository bidRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public LoadResponse createLoad(LoadRequest request) {
        Load load = Load.builder()
                .shipperId(request.getShipperId())
                .loadingCity(request.getLoadingCity())
                .unloadingCity(request.getUnloadingCity())
                .loadingDate(request.getLoadingDate())
                .productType(request.getProductType())
                .weight(request.getWeight())
                .weightUnit(request.getWeightUnit())
                .truckType(request.getTruckType())
                .noOfTrucks(request.getNoOfTrucks())
                .status(LoadStatus.POSTED)
                .build();

        Load savedLoad = loadRepository.save(load);
        return mapToResponse(savedLoad, null);
    }

    public Page<LoadResponse> getLoads(String shipperId, LoadStatus status, Pageable pageable) {
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
        return loads.map(load -> mapToResponse(load, null));
    }

    public LoadResponse getLoadById(UUID loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + loadId));

        List<Bid> activeBids = bidRepository.findByLoadLoadIdAndStatus(loadId, BidStatus.PENDING);
        List<BidResponse> bidResponses = activeBids.stream()
                .map(this::mapBidToResponse)
                .collect(Collectors.toList());

        return mapToResponse(load, bidResponses);
    }

    @Transactional
    public LoadResponse cancelLoad(UUID loadId) {
        Load load = loadRepository.findByIdWithLock(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + loadId));

        if (load.getStatus() == LoadStatus.BOOKED) {
            throw new InvalidStatusTransitionException("Cannot cancel a load that is already BOOKED");
        }

        if (load.getStatus() == LoadStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("Load is already CANCELLED");
        }

        load.setStatus(LoadStatus.CANCELLED);
        Load savedLoad = loadRepository.save(load);

        // Reject all pending bids
        List<Bid> pendingBids = bidRepository.findByLoadLoadIdAndStatus(loadId, BidStatus.PENDING);
        pendingBids.forEach(bid -> {
            bid.setStatus(BidStatus.REJECTED);
            bidRepository.save(bid);
        });

        return mapToResponse(savedLoad, null);
    }

    public List<BidResponse> getBestBids(UUID loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + loadId));

        List<Bid> pendingBids = bidRepository.findPendingBidsWithTransporterByLoadId(loadId);

        return pendingBids.stream()
                .map(bid -> {
                    double transporterRating = bid.getTransporter().getRating();
                    // score = (1 / proposedRate) * 0.7 + (rating / 5) * 0.3
                    double score = (1.0 / bid.getProposedRate()) * 0.7 + (transporterRating / 5.0) * 0.3;
                    BidResponse response = mapBidToResponse(bid);
                    response.setScore(score);
                    return response;
                })
                .sorted((b1, b2) -> Double.compare(b2.getScore(), b1.getScore())) // Higher score first
                .collect(Collectors.toList());
    }

    public int getRemainingTrucks(UUID loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + loadId));

        Integer allocatedTrucks = bookingRepository.sumAllocatedTrucksByLoadId(loadId);
        return load.getNoOfTrucks() - (allocatedTrucks != null ? allocatedTrucks : 0);
    }

    @Transactional
    public void updateLoadStatusOnBid(UUID loadId) {
        Load load = loadRepository.findByIdWithLock(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + loadId));

        if (load.getStatus() == LoadStatus.POSTED) {
            load.setStatus(LoadStatus.OPEN_FOR_BIDS);
            loadRepository.save(load);
        }
    }

    @Transactional
    public void updateLoadStatusOnBooking(UUID loadId) {
        Load load = loadRepository.findByIdWithLock(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + loadId));

        Integer allocatedTrucks = bookingRepository.sumAllocatedTrucksByLoadId(loadId);
        int allocated = allocatedTrucks != null ? allocatedTrucks : 0;

        if (allocated >= load.getNoOfTrucks()) {
            load.setStatus(LoadStatus.BOOKED);
            loadRepository.save(load);
        }
    }

    @Transactional
    public void updateLoadStatusOnBookingCancellation(UUID loadId) {
        Load load = loadRepository.findByIdWithLock(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + loadId));

        if (load.getStatus() == LoadStatus.BOOKED) {
            Integer allocatedTrucks = bookingRepository.sumAllocatedTrucksByLoadId(loadId);
            int allocated = allocatedTrucks != null ? allocatedTrucks : 0;

            if (allocated < load.getNoOfTrucks()) {
                load.setStatus(LoadStatus.OPEN_FOR_BIDS);
                loadRepository.save(load);
            }
        }
    }

    private LoadResponse mapToResponse(Load load, List<BidResponse> activeBids) {
        Integer allocatedTrucks = bookingRepository.sumAllocatedTrucksByLoadId(load.getLoadId());
        int remainingTrucks = load.getNoOfTrucks() - (allocatedTrucks != null ? allocatedTrucks : 0);

        return LoadResponse.builder()
                .loadId(load.getLoadId())
                .shipperId(load.getShipperId())
                .loadingCity(load.getLoadingCity())
                .unloadingCity(load.getUnloadingCity())
                .loadingDate(load.getLoadingDate())
                .productType(load.getProductType())
                .weight(load.getWeight())
                .weightUnit(load.getWeightUnit())
                .truckType(load.getTruckType())
                .noOfTrucks(load.getNoOfTrucks())
                .status(load.getStatus())
                .datePosted(load.getDatePosted())
                .remainingTrucks(remainingTrucks)
                .activeBids(activeBids)
                .build();
    }

    private BidResponse mapBidToResponse(Bid bid) {
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
