package com.tms.service;

import com.tms.dto.*;
import com.tms.entity.*;
import com.tms.exception.*;
import com.tms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BidRepository bidRepository;
    private final LoadRepository loadRepository;
    private final BidService bidService;
    private final TransporterService transporterService;
    private final LoadService loadService;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        Bid bid = bidRepository.findById(request.getBidId())
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + request.getBidId()));

        // Validate bid status
        if (bid.getStatus() != BidStatus.PENDING) {
            throw new InvalidStatusTransitionException("Can only create booking from PENDING bids. Current status: " + bid.getStatus());
        }

        Load load;
        try {
            load = loadRepository.findByIdWithLock(bid.getLoad().getLoadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Load not found"));
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new LoadAlreadyBookedException("Load was modified by another transaction. Please retry.");
        }

        // Validate load status
        if (load.getStatus() == LoadStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("Cannot create booking for a CANCELLED load");
        }
        if (load.getStatus() == LoadStatus.BOOKED) {
            throw new LoadAlreadyBookedException("Load is already fully BOOKED");
        }

        // Validate remaining trucks
        int remainingTrucks = loadService.getRemainingTrucks(load.getLoadId());
        if (bid.getTrucksOffered() > remainingTrucks) {
            throw new InvalidStatusTransitionException(
                    "Trucks offered (" + bid.getTrucksOffered() + ") exceeds remaining trucks needed (" + remainingTrucks + ")");
        }

        // Validate transporter capacity
        int availableTrucks = transporterService.getAvailableTrucks(bid.getTransporter().getTransporterId(), load.getTruckType());
        if (bid.getTrucksOffered() > availableTrucks) {
            throw new InsufficientCapacityException(
                    "Transporter no longer has enough trucks. Required: " + bid.getTrucksOffered() +
                    ", Available: " + availableTrucks);
        }

        // Accept the bid
        bidService.acceptBid(request.getBidId());

        // Deduct trucks from transporter
        transporterService.deductTrucks(bid.getTransporter().getTransporterId(), load.getTruckType(), bid.getTrucksOffered());

        // Create booking
        Booking booking = Booking.builder()
                .load(load)
                .bid(bid)
                .transporter(bid.getTransporter())
                .allocatedTrucks(bid.getTrucksOffered())
                .finalRate(bid.getProposedRate())
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // Update load status if fully booked
        loadService.updateLoadStatusOnBooking(load.getLoadId());

        return mapToResponse(savedBooking);
    }

    public BookingResponse getBookingById(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        return mapToResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("Booking is already CANCELLED");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new InvalidStatusTransitionException("Cannot cancel a COMPLETED booking");
        }

        // Restore trucks to transporter
        String truckType = booking.getLoad().getTruckType();
        transporterService.restoreTrucks(booking.getTransporter().getTransporterId(), truckType, booking.getAllocatedTrucks());

        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(booking);

        // Update load status if needed
        loadService.updateLoadStatusOnBookingCancellation(booking.getLoad().getLoadId());

        return mapToResponse(savedBooking);
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .loadId(booking.getLoad().getLoadId())
                .bidId(booking.getBid().getBidId())
                .transporterId(booking.getTransporter().getTransporterId())
                .transporterCompanyName(booking.getTransporter().getCompanyName())
                .allocatedTrucks(booking.getAllocatedTrucks())
                .finalRate(booking.getFinalRate())
                .status(booking.getStatus())
                .bookedAt(booking.getBookedAt())
                .build();
    }
}
