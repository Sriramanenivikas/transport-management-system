package com.tms.service;

import com.tms.dto.BookingRequest;
import com.tms.dto.BookingResponse;
import com.tms.entity.*;
import com.tms.exception.InsufficientCapacityException;
import com.tms.exception.InvalidStatusTransitionException;
import com.tms.exception.LoadAlreadyBookedException;
import com.tms.exception.ResourceNotFoundException;
import com.tms.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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
    private TransporterRepository transporterRepository;

    @Mock
    private TruckCapacityRepository truckCapacityRepository;

    @InjectMocks
    private BookingService bookingService;

    private Load testLoad;
    private Bid testBid;
    private Transporter testTransporter;
    private TruckCapacity testTruckCapacity;
    private Booking testBooking;
    private BookingRequest testBookingRequest;

    @BeforeEach
    void setUp() {
        testLoad = new Load();
        testLoad.setLoadId(1);
        testLoad.setStatus("OPEN_FOR_BIDS");
        testLoad.setTruckType("CONTAINER-20FT");
        testLoad.setNoOfTrucks(3);

        testTransporter = new Transporter();
        testTransporter.setTransporterId(1);
        testTransporter.setCompanyName("Test Transport");
        testTransporter.setRating(4.5);

        testTruckCapacity = new TruckCapacity("CONTAINER-20FT", 10);
        testTruckCapacity.setTransporter(testTransporter);

        testBid = new Bid();
        testBid.setBidId(1);
        testBid.setLoadId(1);
        testBid.setTransporterId(1);
        testBid.setProposedRate(50000.0);
        testBid.setTrucksOffered(3);
        testBid.setStatus("PENDING");

        testBooking = new Booking();
        testBooking.setBookingId(1);
        testBooking.setLoadId(1);
        testBooking.setBidId(1);
        testBooking.setTransporterId(1);
        testBooking.setAllocatedTrucks(3);
        testBooking.setFinalRate(50000.0);
        testBooking.setStatus("CONFIRMED");

        testBookingRequest = new BookingRequest();
        testBookingRequest.setBidId(1);
        testBookingRequest.setAllocatedTrucks(3);
    }

    @Test
    void createBooking_Success() {
        when(bidRepository.findById(1)).thenReturn(Optional.of(testBid));
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));
        when(transporterRepository.findById(1)).thenReturn(Optional.of(testTransporter));
        when(truckCapacityRepository.findByTransporterTransporterIdAndTruckType(1, "CONTAINER-20FT"))
                .thenReturn(Optional.of(testTruckCapacity));
        when(bookingRepository.getTotalAllocatedTrucks(1)).thenReturn(0);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bidRepository.save(any(Bid.class))).thenReturn(testBid);
        when(loadRepository.save(any(Load.class))).thenReturn(testLoad);
        when(truckCapacityRepository.save(any(TruckCapacity.class))).thenReturn(testTruckCapacity);

        BookingResponse response = bookingService.createBooking(testBookingRequest);

        assertNotNull(response);
        assertEquals("CONFIRMED", response.getStatus());
        assertEquals(50000.0, response.getFinalRate());
    }

    @Test
    void createBooking_BidNotPending_ThrowsException() {
        testBid.setStatus("ACCEPTED");
        when(bidRepository.findById(1)).thenReturn(Optional.of(testBid));

        assertThrows(InvalidStatusTransitionException.class, () -> bookingService.createBooking(testBookingRequest));
    }

    @Test
    void createBooking_LoadCancelled_ThrowsException() {
        testLoad.setStatus("CANCELLED");
        when(bidRepository.findById(1)).thenReturn(Optional.of(testBid));
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));

        assertThrows(InvalidStatusTransitionException.class, () -> bookingService.createBooking(testBookingRequest));
    }

    @Test
    void createBooking_ExceedsOfferedTrucks_ThrowsException() {
        testBookingRequest.setAllocatedTrucks(5);
        when(bidRepository.findById(1)).thenReturn(Optional.of(testBid));
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));

        assertThrows(InsufficientCapacityException.class, () -> bookingService.createBooking(testBookingRequest));
    }

    @Test
    void createBooking_ExceedsRemainingTrucks_ThrowsException() {
        when(bidRepository.findById(1)).thenReturn(Optional.of(testBid));
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));
        when(bookingRepository.getTotalAllocatedTrucks(1)).thenReturn(2);

        assertThrows(LoadAlreadyBookedException.class, () -> bookingService.createBooking(testBookingRequest));
    }

    @Test
    void createBooking_InsufficientTransporterCapacity_ThrowsException() {
        testTruckCapacity.setCount(1);
        when(bidRepository.findById(1)).thenReturn(Optional.of(testBid));
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));
        when(transporterRepository.findById(1)).thenReturn(Optional.of(testTransporter));
        when(truckCapacityRepository.findByTransporterTransporterIdAndTruckType(1, "CONTAINER-20FT"))
                .thenReturn(Optional.of(testTruckCapacity));
        when(bookingRepository.getTotalAllocatedTrucks(1)).thenReturn(0);

        assertThrows(InsufficientCapacityException.class, () -> bookingService.createBooking(testBookingRequest));
    }

    @Test
    void createBooking_UpdatesLoadStatusToBooked_WhenFullyAllocated() {
        when(bidRepository.findById(1)).thenReturn(Optional.of(testBid));
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));
        when(transporterRepository.findById(1)).thenReturn(Optional.of(testTransporter));
        when(truckCapacityRepository.findByTransporterTransporterIdAndTruckType(1, "CONTAINER-20FT"))
                .thenReturn(Optional.of(testTruckCapacity));
        when(bookingRepository.getTotalAllocatedTrucks(1)).thenReturn(0);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bidRepository.save(any(Bid.class))).thenReturn(testBid);
        when(loadRepository.save(any(Load.class))).thenAnswer(inv -> {
            Load load = inv.getArgument(0);
            assertEquals("BOOKED", load.getStatus());
            return load;
        });
        when(truckCapacityRepository.save(any(TruckCapacity.class))).thenReturn(testTruckCapacity);

        bookingService.createBooking(testBookingRequest);

        verify(loadRepository).save(any(Load.class));
    }

    @Test
    void createBooking_DeductsTruckCapacity() {
        when(bidRepository.findById(1)).thenReturn(Optional.of(testBid));
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));
        when(transporterRepository.findById(1)).thenReturn(Optional.of(testTransporter));
        when(truckCapacityRepository.findByTransporterTransporterIdAndTruckType(1, "CONTAINER-20FT"))
                .thenReturn(Optional.of(testTruckCapacity));
        when(bookingRepository.getTotalAllocatedTrucks(1)).thenReturn(0);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bidRepository.save(any(Bid.class))).thenReturn(testBid);
        when(loadRepository.save(any(Load.class))).thenReturn(testLoad);
        when(truckCapacityRepository.save(any(TruckCapacity.class))).thenAnswer(inv -> {
            TruckCapacity tc = inv.getArgument(0);
            assertEquals(7, tc.getCount());
            return tc;
        });

        bookingService.createBooking(testBookingRequest);

        verify(truckCapacityRepository).save(any(TruckCapacity.class));
    }

    @Test
    void cancelBooking_Success() {
        when(bookingRepository.findById(1)).thenReturn(Optional.of(testBooking));
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));
        when(truckCapacityRepository.findByTransporterTransporterIdAndTruckType(1, "CONTAINER-20FT"))
                .thenReturn(Optional.of(testTruckCapacity));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking booking = inv.getArgument(0);
            booking.setStatus("CANCELLED");
            return booking;
        });
        when(truckCapacityRepository.save(any(TruckCapacity.class))).thenReturn(testTruckCapacity);
        when(transporterRepository.findById(1)).thenReturn(Optional.of(testTransporter));

        BookingResponse response = bookingService.cancelBooking(1);

        assertEquals("CANCELLED", response.getStatus());
    }

    @Test
    void cancelBooking_AlreadyCancelled_ThrowsException() {
        testBooking.setStatus("CANCELLED");
        when(bookingRepository.findById(1)).thenReturn(Optional.of(testBooking));

        assertThrows(InvalidStatusTransitionException.class, () -> bookingService.cancelBooking(1));
    }

    @Test
    void cancelBooking_RestoresTruckCapacity() {
        testTruckCapacity.setCount(7);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(testBooking));
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));
        when(truckCapacityRepository.findByTransporterTransporterIdAndTruckType(1, "CONTAINER-20FT"))
                .thenReturn(Optional.of(testTruckCapacity));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(truckCapacityRepository.save(any(TruckCapacity.class))).thenAnswer(inv -> {
            TruckCapacity tc = inv.getArgument(0);
            assertEquals(10, tc.getCount());
            return tc;
        });
        when(transporterRepository.findById(1)).thenReturn(Optional.of(testTransporter));

        bookingService.cancelBooking(1);

        verify(truckCapacityRepository).save(any(TruckCapacity.class));
    }

    @Test
    void cancelBooking_UpdatesLoadStatusFromBooked() {
        testLoad.setStatus("BOOKED");
        when(bookingRepository.findById(1)).thenReturn(Optional.of(testBooking));
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));
        when(truckCapacityRepository.findByTransporterTransporterIdAndTruckType(1, "CONTAINER-20FT"))
                .thenReturn(Optional.of(testTruckCapacity));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(truckCapacityRepository.save(any(TruckCapacity.class))).thenReturn(testTruckCapacity);
        when(loadRepository.save(any(Load.class))).thenAnswer(inv -> {
            Load load = inv.getArgument(0);
            assertEquals("OPEN_FOR_BIDS", load.getStatus());
            return load;
        });
        when(transporterRepository.findById(1)).thenReturn(Optional.of(testTransporter));

        bookingService.cancelBooking(1);

        verify(loadRepository).save(any(Load.class));
    }

    @Test
    void getBookingById_Success() {
        when(bookingRepository.findById(1)).thenReturn(Optional.of(testBooking));
        when(transporterRepository.findById(1)).thenReturn(Optional.of(testTransporter));

        BookingResponse response = bookingService.getBookingById(1);

        assertNotNull(response);
        assertEquals(1, response.getBookingId());
    }

    @Test
    void getBookingById_NotFound_ThrowsException() {
        when(bookingRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingService.getBookingById(999));
    }
}
