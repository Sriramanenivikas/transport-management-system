package com.tms.service;

import com.tms.dto.BookingRequest;
import com.tms.dto.BookingResponse;
import com.tms.entity.*;
import com.tms.exception.*;
import com.tms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BidRepository bidRepository;
    private final LoadRepository loadRepository;
    private final TransporterRepository transporterRepository;
    private final TruckCapacityRepository truckCapacityRepository;

    public BookingService(BookingRepository bookingRepository, BidRepository bidRepository,
                         LoadRepository loadRepository, TransporterRepository transporterRepository,
                         TruckCapacityRepository truckCapacityRepository) {
        this.bookingRepository = bookingRepository;
        this.bidRepository = bidRepository;
        this.loadRepository = loadRepository;
        this.transporterRepository = transporterRepository;
        this.truckCapacityRepository = truckCapacityRepository;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        Bid bid = bidRepository.findById(request.getBidId())
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with ID: " + request.getBidId()));

        if (!"PENDING".equals(bid.getStatus())) {
            throw new InvalidStatusTransitionException("Can only accept bids with PENDING status");
        }

        Load load = loadRepository.findById(bid.getLoadId())
                .orElseThrow(() -> new ResourceNotFoundException("Load not found"));

        if ("CANCELLED".equals(load.getStatus())) {
            throw new InvalidStatusTransitionException("Cannot book a cancelled load");
        }

        if (request.getAllocatedTrucks() > bid.getTrucksOffered()) {
            throw new InsufficientCapacityException("Cannot allocate more trucks than offered in bid");
        }

        Integer currentlyAllocated = bookingRepository.getTotalAllocatedTrucks(load.getLoadId());
        int remaining = load.getNoOfTrucks() - (currentlyAllocated != null ? currentlyAllocated : 0);

        if (request.getAllocatedTrucks() > remaining) {
            throw new LoadAlreadyBookedException(
                    String.format("Only %d trucks remaining, but trying to allocate %d",
                            remaining, request.getAllocatedTrucks()));
        }

        Transporter transporter = transporterRepository.findById(bid.getTransporterId())
                .orElseThrow(() -> new ResourceNotFoundException("Transporter not found"));

        TruckCapacity capacity = truckCapacityRepository
                .findByTransporterTransporterIdAndTruckType(bid.getTransporterId(), load.getTruckType())
                .orElseThrow(() -> new InsufficientCapacityException("Truck type not available"));

        if (capacity.getCount() < request.getAllocatedTrucks()) {
            throw new InsufficientCapacityException(
                    String.format("Transporter only has %d trucks available", capacity.getCount()));
        }

        capacity.setCount(capacity.getCount() - request.getAllocatedTrucks());
        truckCapacityRepository.save(capacity);

        Booking booking = new Booking();
        booking.setLoadId(load.getLoadId());
        booking.setBidId(bid.getBidId());
        booking.setTransporterId(bid.getTransporterId());
        booking.setAllocatedTrucks(request.getAllocatedTrucks());
        booking.setFinalRate(bid.getProposedRate());
        booking.setStatus("CONFIRMED");

        booking = bookingRepository.save(booking);

        bid.setStatus("ACCEPTED");
        bidRepository.save(bid);

        int newAllocated = (currentlyAllocated != null ? currentlyAllocated : 0) + request.getAllocatedTrucks();
        if (newAllocated >= load.getNoOfTrucks()) {
            load.setStatus("BOOKED");
            loadRepository.save(load);
        }

        return toBookingResponse(booking, transporter);
    }

    public BookingResponse getBookingById(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        Transporter transporter = transporterRepository.findById(booking.getTransporterId()).orElse(null);
        return toBookingResponse(booking, transporter);
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(booking -> {
                    Transporter t = transporterRepository.findById(booking.getTransporterId()).orElse(null);
                    return toBookingResponse(booking, t);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponse cancelBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new InvalidStatusTransitionException("Booking is already cancelled");
        }

        Load load = loadRepository.findById(booking.getLoadId()).orElseThrow();
        TruckCapacity capacity = truckCapacityRepository
                .findByTransporterTransporterIdAndTruckType(booking.getTransporterId(), load.getTruckType())
                .orElseThrow();

        capacity.setCount(capacity.getCount() + booking.getAllocatedTrucks());
        truckCapacityRepository.save(capacity);

        booking.setStatus("CANCELLED");
        booking = bookingRepository.save(booking);

        if ("BOOKED".equals(load.getStatus())) {
            load.setStatus("OPEN_FOR_BIDS");
            loadRepository.save(load);
        }

        Transporter transporter = transporterRepository.findById(booking.getTransporterId()).orElse(null);
        return toBookingResponse(booking, transporter);
    }

    private BookingResponse toBookingResponse(Booking booking, Transporter transporter) {
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getBookingId());
        response.setLoadId(booking.getLoadId());
        response.setBidId(booking.getBidId());
        response.setTransporterId(booking.getTransporterId());
        response.setAllocatedTrucks(booking.getAllocatedTrucks());
        response.setFinalRate(booking.getFinalRate());
        response.setStatus(booking.getStatus());
        response.setBookedAt(booking.getBookedAt());

        if (transporter != null) {
            response.setTransporterCompanyName(transporter.getCompanyName());
        }

        return response;
    }
}

