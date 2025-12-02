package com.tms.service;

import com.tms.dto.BidRequest;
import com.tms.dto.BidResponse;
import com.tms.entity.Bid;
import com.tms.entity.Load;
import com.tms.entity.Transporter;
import com.tms.entity.TruckCapacity;
import com.tms.exception.InsufficientCapacityException;
import com.tms.exception.InvalidStatusTransitionException;
import com.tms.exception.ResourceNotFoundException;
import com.tms.repository.BidRepository;
import com.tms.repository.LoadRepository;
import com.tms.repository.TransporterRepository;
import com.tms.repository.TruckCapacityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private LoadRepository loadRepository;

    @Mock
    private TransporterRepository transporterRepository;

    @Mock
    private TruckCapacityRepository truckCapacityRepository;

    @InjectMocks
    private BidService bidService;

    private Load testLoad;
    private Transporter testTransporter;
    private TruckCapacity testTruckCapacity;
    private Bid testBid;
    private BidRequest testBidRequest;

    @BeforeEach
    void setUp() {
        testLoad = new Load();
        testLoad.setLoadId(1);
        testLoad.setStatus("POSTED");
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

        testBidRequest = new BidRequest();
        testBidRequest.setLoadId(1);
        testBidRequest.setTransporterId(1);
        testBidRequest.setProposedRate(50000.0);
        testBidRequest.setTrucksOffered(3);
    }

    @Test
    void createBid_Success() {
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));
        when(transporterRepository.findById(1)).thenReturn(Optional.of(testTransporter));
        when(truckCapacityRepository.findByTransporterTransporterIdAndTruckType(1, "CONTAINER-20FT"))
                .thenReturn(Optional.of(testTruckCapacity));
        when(bidRepository.save(any(Bid.class))).thenReturn(testBid);
        when(loadRepository.save(any(Load.class))).thenReturn(testLoad);

        BidResponse response = bidService.createBid(testBidRequest);

        assertNotNull(response);
        assertEquals(50000.0, response.getProposedRate());
        assertEquals("PENDING", response.getStatus());
    }

    @Test
    void createBid_LoadNotFound_ThrowsException() {
        when(loadRepository.findById(999)).thenReturn(Optional.empty());
        testBidRequest.setLoadId(999);

        assertThrows(ResourceNotFoundException.class, () -> bidService.createBid(testBidRequest));
    }

    @Test
    void createBid_LoadCancelled_ThrowsException() {
        testLoad.setStatus("CANCELLED");
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));

        assertThrows(InvalidStatusTransitionException.class, () -> bidService.createBid(testBidRequest));
    }

    @Test
    void createBid_LoadBooked_ThrowsException() {
        testLoad.setStatus("BOOKED");
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));

        assertThrows(InvalidStatusTransitionException.class, () -> bidService.createBid(testBidRequest));
    }

    @Test
    void createBid_InsufficientCapacity_ThrowsException() {
        testTruckCapacity.setCount(1);
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));
        when(transporterRepository.findById(1)).thenReturn(Optional.of(testTransporter));
        when(truckCapacityRepository.findByTransporterTransporterIdAndTruckType(1, "CONTAINER-20FT"))
                .thenReturn(Optional.of(testTruckCapacity));

        assertThrows(InsufficientCapacityException.class, () -> bidService.createBid(testBidRequest));
    }

    @Test
    void createBid_NoTruckType_ThrowsException() {
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));
        when(transporterRepository.findById(1)).thenReturn(Optional.of(testTransporter));
        when(truckCapacityRepository.findByTransporterTransporterIdAndTruckType(1, "CONTAINER-20FT"))
                .thenReturn(Optional.empty());

        assertThrows(InsufficientCapacityException.class, () -> bidService.createBid(testBidRequest));
    }

    @Test
    void createBid_UpdatesLoadStatusToOpenForBids() {
        testLoad.setStatus("POSTED");
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));
        when(transporterRepository.findById(1)).thenReturn(Optional.of(testTransporter));
        when(truckCapacityRepository.findByTransporterTransporterIdAndTruckType(1, "CONTAINER-20FT"))
                .thenReturn(Optional.of(testTruckCapacity));
        when(bidRepository.save(any(Bid.class))).thenReturn(testBid);
        when(loadRepository.save(any(Load.class))).thenAnswer(inv -> {
            Load load = inv.getArgument(0);
            assertEquals("OPEN_FOR_BIDS", load.getStatus());
            return load;
        });

        bidService.createBid(testBidRequest);

        verify(loadRepository).save(any(Load.class));
    }

    @Test
    void rejectBid_Success() {
        when(bidRepository.findById(1)).thenReturn(Optional.of(testBid));
        when(bidRepository.save(any(Bid.class))).thenAnswer(inv -> {
            Bid bid = inv.getArgument(0);
            bid.setStatus("REJECTED");
            return bid;
        });
        when(transporterRepository.findById(1)).thenReturn(Optional.of(testTransporter));

        BidResponse response = bidService.rejectBid(1);

        assertEquals("REJECTED", response.getStatus());
    }

    @Test
    void rejectBid_AlreadyRejected_ThrowsException() {
        testBid.setStatus("REJECTED");
        when(bidRepository.findById(1)).thenReturn(Optional.of(testBid));

        assertThrows(InvalidStatusTransitionException.class, () -> bidService.rejectBid(1));
    }

    @Test
    void rejectBid_AlreadyAccepted_ThrowsException() {
        testBid.setStatus("ACCEPTED");
        when(bidRepository.findById(1)).thenReturn(Optional.of(testBid));

        assertThrows(InvalidStatusTransitionException.class, () -> bidService.rejectBid(1));
    }

    @Test
    void getBids_FilterByLoadId() {
        when(bidRepository.findByLoadId(1)).thenReturn(Collections.singletonList(testBid));
        when(transporterRepository.findById(any())).thenReturn(Optional.of(testTransporter));

        List<BidResponse> response = bidService.getBids(1, null, null);

        assertEquals(1, response.size());
    }

    @Test
    void getBids_FilterByStatus() {
        when(bidRepository.findByStatus("PENDING")).thenReturn(Collections.singletonList(testBid));
        when(transporterRepository.findById(any())).thenReturn(Optional.of(testTransporter));

        List<BidResponse> response = bidService.getBids(null, null, "PENDING");

        assertEquals(1, response.size());
    }
}
