package com.tms.service;

import com.tms.dto.*;
import com.tms.entity.*;
import com.tms.exception.*;
import com.tms.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private LoadRepository loadRepository;

    @Mock
    private BidService bidService;

    @Mock
    private TransporterService transporterService;

    @Mock
    private LoadService loadService;

    @InjectMocks
    private BookingService bookingService;

    private Load testLoad;
    private Transporter testTransporter;
    private Bid testBid;
    private Booking testBooking;
    private BookingRequest testBookingRequest;

    @BeforeEach
    void setUp() {
        testLoad = Load.builder()
                .loadId(UUID.randomUUID())
                .shipperId("SHIP001")
                .loadingCity("Mumbai")
                .unloadingCity("Delhi")
                .loadingDate(LocalDateTime.now().plusDays(1))
                .productType("Electronics")
                .weight(5000.0)
                .weightUnit(WeightUnit.KG)
                .truckType("Container")
                .noOfTrucks(3)
                .status(LoadStatus.OPEN_FOR_BIDS)
                .datePosted(LocalDateTime.now())
                .build();

        testTransporter = Transporter.builder()
                .transporterId(UUID.randomUUID())
                .companyName("ABC Transport")
                .rating(4.5)
                .availableTrucks(new ArrayList<>())
                .build();

        testBid = Bid.builder()
                .bidId(UUID.randomUUID())
                .load(testLoad)
                .transporter(testTransporter)
                .proposedRate(50000.0)
                .trucksOffered(2)
                .status(BidStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();

        testBooking = Booking.builder()
                .bookingId(UUID.randomUUID())
                .load(testLoad)
                .bid(testBid)
                .transporter(testTransporter)
                .allocatedTrucks(2)
                .finalRate(50000.0)
                .status(BookingStatus.CONFIRMED)
                .bookedAt(LocalDateTime.now())
                .build();

        testBookingRequest = BookingRequest.builder()
                .bidId(testBid.getBidId())
                .build();
    }

    @Test
    void createBooking_Success() {
        when(bidRepository.findById(testBid.getBidId())).thenReturn(Optional.of(testBid));
        when(loadRepository.findByIdWithLock(testLoad.getLoadId())).thenReturn(Optional.of(testLoad));
        when(loadService.getRemainingTrucks(testLoad.getLoadId())).thenReturn(3);
        when(transporterService.getAvailableTrucks(testTransporter.getTransporterId(), "Container")).thenReturn(10);
        doNothing().when(bidService).acceptBid(testBid.getBidId());
        doNothing().when(transporterService).deductTrucks(testTransporter.getTransporterId(), "Container", 2);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        doNothing().when(loadService).updateLoadStatusOnBooking(testLoad.getLoadId());

        BookingResponse response = bookingService.createBooking(testBookingRequest);

        assertNotNull(response);
        assertEquals(testBooking.getBookingId(), response.getBookingId());
        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(bidService, times(1)).acceptBid(testBid.getBidId());
        verify(transporterService, times(1)).deductTrucks(testTransporter.getTransporterId(), "Container", 2);
    }

    @Test
    void createBooking_BidNotPending_ThrowsException() {
        testBid.setStatus(BidStatus.REJECTED);
        when(bidRepository.findById(testBid.getBidId())).thenReturn(Optional.of(testBid));

        assertThrows(InvalidStatusTransitionException.class, () -> bookingService.createBooking(testBookingRequest));
    }

    @Test
    void createBooking_LoadCancelled_ThrowsException() {
        testLoad.setStatus(LoadStatus.CANCELLED);
        when(bidRepository.findById(testBid.getBidId())).thenReturn(Optional.of(testBid));
        when(loadRepository.findByIdWithLock(testLoad.getLoadId())).thenReturn(Optional.of(testLoad));

        assertThrows(InvalidStatusTransitionException.class, () -> bookingService.createBooking(testBookingRequest));
    }

    @Test
    void createBooking_LoadAlreadyBooked_ThrowsException() {
        testLoad.setStatus(LoadStatus.BOOKED);
        when(bidRepository.findById(testBid.getBidId())).thenReturn(Optional.of(testBid));
        when(loadRepository.findByIdWithLock(testLoad.getLoadId())).thenReturn(Optional.of(testLoad));

        assertThrows(LoadAlreadyBookedException.class, () -> bookingService.createBooking(testBookingRequest));
    }

    @Test
    void createBooking_ExceedsRemainingTrucks_ThrowsException() {
        when(bidRepository.findById(testBid.getBidId())).thenReturn(Optional.of(testBid));
        when(loadRepository.findByIdWithLock(testLoad.getLoadId())).thenReturn(Optional.of(testLoad));
        when(loadService.getRemainingTrucks(testLoad.getLoadId())).thenReturn(1);

        assertThrows(InvalidStatusTransitionException.class, () -> bookingService.createBooking(testBookingRequest));
    }

    @Test
    void createBooking_InsufficientTransporterCapacity_ThrowsException() {
        when(bidRepository.findById(testBid.getBidId())).thenReturn(Optional.of(testBid));
        when(loadRepository.findByIdWithLock(testLoad.getLoadId())).thenReturn(Optional.of(testLoad));
        when(loadService.getRemainingTrucks(testLoad.getLoadId())).thenReturn(3);
        when(transporterService.getAvailableTrucks(testTransporter.getTransporterId(), "Container")).thenReturn(1);

        assertThrows(InsufficientCapacityException.class, () -> bookingService.createBooking(testBookingRequest));
    }

    @Test
    void getBookingById_Success() {
        when(bookingRepository.findById(testBooking.getBookingId())).thenReturn(Optional.of(testBooking));

        BookingResponse response = bookingService.getBookingById(testBooking.getBookingId());

        assertNotNull(response);
        assertEquals(testBooking.getBookingId(), response.getBookingId());
    }

    @Test
    void getBookingById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(bookingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingService.getBookingById(nonExistentId));
    }

    @Test
    void cancelBooking_Success() {
        when(bookingRepository.findById(testBooking.getBookingId())).thenReturn(Optional.of(testBooking));
        doNothing().when(transporterService).restoreTrucks(testTransporter.getTransporterId(), "Container", 2);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        doNothing().when(loadService).updateLoadStatusOnBookingCancellation(testLoad.getLoadId());

        BookingResponse response = bookingService.cancelBooking(testBooking.getBookingId());

        assertEquals(BookingStatus.CANCELLED, testBooking.getStatus());
        verify(transporterService, times(1)).restoreTrucks(testTransporter.getTransporterId(), "Container", 2);
        verify(loadService, times(1)).updateLoadStatusOnBookingCancellation(testLoad.getLoadId());
    }

    @Test
    void cancelBooking_AlreadyCancelled_ThrowsException() {
        testBooking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(testBooking.getBookingId())).thenReturn(Optional.of(testBooking));

        assertThrows(InvalidStatusTransitionException.class, () -> bookingService.cancelBooking(testBooking.getBookingId()));
    }

    @Test
    void cancelBooking_Completed_ThrowsException() {
        testBooking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findById(testBooking.getBookingId())).thenReturn(Optional.of(testBooking));

        assertThrows(InvalidStatusTransitionException.class, () -> bookingService.cancelBooking(testBooking.getBookingId()));
    }
}
