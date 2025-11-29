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
class BidServiceTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private LoadRepository loadRepository;

    @Mock
    private TransporterRepository transporterRepository;

    @Mock
    private TransporterService transporterService;

    @Mock
    private LoadService loadService;

    @InjectMocks
    private BidService bidService;

    private Load testLoad;
    private Transporter testTransporter;
    private Bid testBid;
    private BidRequest testBidRequest;

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
                .status(LoadStatus.POSTED)
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

        testBidRequest = BidRequest.builder()
                .loadId(testLoad.getLoadId())
                .transporterId(testTransporter.getTransporterId())
                .proposedRate(50000.0)
                .trucksOffered(2)
                .build();
    }

    @Test
    void submitBid_Success() {
        when(loadRepository.findById(testLoad.getLoadId())).thenReturn(Optional.of(testLoad));
        when(transporterRepository.findById(testTransporter.getTransporterId())).thenReturn(Optional.of(testTransporter));
        when(transporterService.getAvailableTrucks(testTransporter.getTransporterId(), "Container")).thenReturn(10);
        when(loadService.getRemainingTrucks(testLoad.getLoadId())).thenReturn(3);
        when(bidRepository.save(any(Bid.class))).thenReturn(testBid);
        doNothing().when(loadService).updateLoadStatusOnBid(testLoad.getLoadId());

        BidResponse response = bidService.submitBid(testBidRequest);

        assertNotNull(response);
        assertEquals(testBid.getBidId(), response.getBidId());
        assertEquals(BidStatus.PENDING, response.getStatus());
        verify(bidRepository, times(1)).save(any(Bid.class));
        verify(loadService, times(1)).updateLoadStatusOnBid(testLoad.getLoadId());
    }

    @Test
    void submitBid_LoadCancelled_ThrowsException() {
        testLoad.setStatus(LoadStatus.CANCELLED);
        when(loadRepository.findById(testLoad.getLoadId())).thenReturn(Optional.of(testLoad));

        assertThrows(InvalidStatusTransitionException.class, () -> bidService.submitBid(testBidRequest));
    }

    @Test
    void submitBid_LoadBooked_ThrowsException() {
        testLoad.setStatus(LoadStatus.BOOKED);
        when(loadRepository.findById(testLoad.getLoadId())).thenReturn(Optional.of(testLoad));

        assertThrows(InvalidStatusTransitionException.class, () -> bidService.submitBid(testBidRequest));
    }

    @Test
    void submitBid_InsufficientCapacity_ThrowsException() {
        when(loadRepository.findById(testLoad.getLoadId())).thenReturn(Optional.of(testLoad));
        when(transporterRepository.findById(testTransporter.getTransporterId())).thenReturn(Optional.of(testTransporter));
        when(transporterService.getAvailableTrucks(testTransporter.getTransporterId(), "Container")).thenReturn(1);

        assertThrows(InsufficientCapacityException.class, () -> bidService.submitBid(testBidRequest));
    }

    @Test
    void submitBid_ExceedsRemainingTrucks_ThrowsException() {
        when(loadRepository.findById(testLoad.getLoadId())).thenReturn(Optional.of(testLoad));
        when(transporterRepository.findById(testTransporter.getTransporterId())).thenReturn(Optional.of(testTransporter));
        when(transporterService.getAvailableTrucks(testTransporter.getTransporterId(), "Container")).thenReturn(10);
        when(loadService.getRemainingTrucks(testLoad.getLoadId())).thenReturn(1);

        assertThrows(InvalidStatusTransitionException.class, () -> bidService.submitBid(testBidRequest));
    }

    @Test
    void getBidById_Success() {
        when(bidRepository.findById(testBid.getBidId())).thenReturn(Optional.of(testBid));

        BidResponse response = bidService.getBidById(testBid.getBidId());

        assertNotNull(response);
        assertEquals(testBid.getBidId(), response.getBidId());
    }

    @Test
    void getBidById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(bidRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bidService.getBidById(nonExistentId));
    }

    @Test
    void rejectBid_Success() {
        when(bidRepository.findById(testBid.getBidId())).thenReturn(Optional.of(testBid));
        when(bidRepository.save(any(Bid.class))).thenReturn(testBid);

        BidResponse response = bidService.rejectBid(testBid.getBidId());

        assertEquals(BidStatus.REJECTED, testBid.getStatus());
        verify(bidRepository, times(1)).save(testBid);
    }

    @Test
    void rejectBid_NotPending_ThrowsException() {
        testBid.setStatus(BidStatus.ACCEPTED);
        when(bidRepository.findById(testBid.getBidId())).thenReturn(Optional.of(testBid));

        assertThrows(InvalidStatusTransitionException.class, () -> bidService.rejectBid(testBid.getBidId()));
    }

    @Test
    void acceptBid_Success() {
        when(bidRepository.findById(testBid.getBidId())).thenReturn(Optional.of(testBid));
        when(bidRepository.save(any(Bid.class))).thenReturn(testBid);

        bidService.acceptBid(testBid.getBidId());

        assertEquals(BidStatus.ACCEPTED, testBid.getStatus());
        verify(bidRepository, times(1)).save(testBid);
    }

    @Test
    void acceptBid_NotPending_ThrowsException() {
        testBid.setStatus(BidStatus.REJECTED);
        when(bidRepository.findById(testBid.getBidId())).thenReturn(Optional.of(testBid));

        assertThrows(InvalidStatusTransitionException.class, () -> bidService.acceptBid(testBid.getBidId()));
    }

    @Test
    void getBids_ByLoadId() {
        when(bidRepository.findByLoadLoadId(testLoad.getLoadId())).thenReturn(List.of(testBid));

        List<BidResponse> response = bidService.getBids(testLoad.getLoadId(), null, null);

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void getBids_ByStatus() {
        when(bidRepository.findByStatus(BidStatus.PENDING)).thenReturn(List.of(testBid));

        List<BidResponse> response = bidService.getBids(null, null, BidStatus.PENDING);

        assertNotNull(response);
        assertEquals(1, response.size());
    }
}
