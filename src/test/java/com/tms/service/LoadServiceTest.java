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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoadServiceTest {

    @Mock
    private LoadRepository loadRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private LoadService loadService;

    private Load testLoad;
    private LoadRequest testLoadRequest;

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

        testLoadRequest = LoadRequest.builder()
                .shipperId("SHIP001")
                .loadingCity("Mumbai")
                .unloadingCity("Delhi")
                .loadingDate(LocalDateTime.now().plusDays(1))
                .productType("Electronics")
                .weight(5000.0)
                .weightUnit(WeightUnit.KG)
                .truckType("Container")
                .noOfTrucks(3)
                .build();
    }

    @Test
    void createLoad_Success() {
        when(loadRepository.save(any(Load.class))).thenReturn(testLoad);
        when(bookingRepository.sumAllocatedTrucksByLoadId(any())).thenReturn(0);

        LoadResponse response = loadService.createLoad(testLoadRequest);

        assertNotNull(response);
        assertEquals("SHIP001", response.getShipperId());
        assertEquals(LoadStatus.POSTED, response.getStatus());
        assertEquals(3, response.getRemainingTrucks());
        verify(loadRepository, times(1)).save(any(Load.class));
    }

    @Test
    void getLoadById_Success() {
        when(loadRepository.findById(testLoad.getLoadId())).thenReturn(Optional.of(testLoad));
        when(bidRepository.findByLoadLoadIdAndStatus(testLoad.getLoadId(), BidStatus.PENDING)).thenReturn(new ArrayList<>());
        when(bookingRepository.sumAllocatedTrucksByLoadId(testLoad.getLoadId())).thenReturn(0);

        LoadResponse response = loadService.getLoadById(testLoad.getLoadId());

        assertNotNull(response);
        assertEquals(testLoad.getLoadId(), response.getLoadId());
        verify(loadRepository, times(1)).findById(testLoad.getLoadId());
    }

    @Test
    void getLoadById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(loadRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> loadService.getLoadById(nonExistentId));
    }

    @Test
    void getLoads_WithFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Load> loadPage = new PageImpl<>(List.of(testLoad), pageable, 1);

        when(loadRepository.findByShipperIdAndStatus("SHIP001", LoadStatus.POSTED, pageable)).thenReturn(loadPage);
        when(bookingRepository.sumAllocatedTrucksByLoadId(any())).thenReturn(0);

        Page<LoadResponse> response = loadService.getLoads("SHIP001", LoadStatus.POSTED, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(loadRepository, times(1)).findByShipperIdAndStatus("SHIP001", LoadStatus.POSTED, pageable);
    }

    @Test
    void cancelLoad_Success() {
        testLoad.setStatus(LoadStatus.OPEN_FOR_BIDS);
        when(loadRepository.findByIdWithLock(testLoad.getLoadId())).thenReturn(Optional.of(testLoad));
        when(loadRepository.save(any(Load.class))).thenReturn(testLoad);
        when(bidRepository.findByLoadLoadIdAndStatus(testLoad.getLoadId(), BidStatus.PENDING)).thenReturn(new ArrayList<>());
        when(bookingRepository.sumAllocatedTrucksByLoadId(testLoad.getLoadId())).thenReturn(0);

        LoadResponse response = loadService.cancelLoad(testLoad.getLoadId());

        assertNotNull(response);
        assertEquals(LoadStatus.CANCELLED, testLoad.getStatus());
        verify(loadRepository, times(1)).save(testLoad);
    }

    @Test
    void cancelLoad_AlreadyBooked_ThrowsException() {
        testLoad.setStatus(LoadStatus.BOOKED);
        when(loadRepository.findByIdWithLock(testLoad.getLoadId())).thenReturn(Optional.of(testLoad));

        assertThrows(InvalidStatusTransitionException.class, () -> loadService.cancelLoad(testLoad.getLoadId()));
    }

    @Test
    void cancelLoad_AlreadyCancelled_ThrowsException() {
        testLoad.setStatus(LoadStatus.CANCELLED);
        when(loadRepository.findByIdWithLock(testLoad.getLoadId())).thenReturn(Optional.of(testLoad));

        assertThrows(InvalidStatusTransitionException.class, () -> loadService.cancelLoad(testLoad.getLoadId()));
    }

    @Test
    void getBestBids_Success() {
        Transporter transporter = Transporter.builder()
                .transporterId(UUID.randomUUID())
                .companyName("ABC Transport")
                .rating(4.5)
                .build();

        Bid bid = Bid.builder()
                .bidId(UUID.randomUUID())
                .load(testLoad)
                .transporter(transporter)
                .proposedRate(50000.0)
                .trucksOffered(2)
                .status(BidStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();

        when(loadRepository.findById(testLoad.getLoadId())).thenReturn(Optional.of(testLoad));
        when(bidRepository.findPendingBidsWithTransporterByLoadId(testLoad.getLoadId())).thenReturn(List.of(bid));

        List<BidResponse> response = loadService.getBestBids(testLoad.getLoadId());

        assertNotNull(response);
        assertEquals(1, response.size());
        assertNotNull(response.get(0).getScore());
        // score = (1 / 50000) * 0.7 + (4.5 / 5) * 0.3 = 0.000014 + 0.27 ≈ 0.270014
        assertTrue(response.get(0).getScore() > 0.27);
    }

    @Test
    void getRemainingTrucks_Success() {
        when(loadRepository.findById(testLoad.getLoadId())).thenReturn(Optional.of(testLoad));
        when(bookingRepository.sumAllocatedTrucksByLoadId(testLoad.getLoadId())).thenReturn(1);

        int remainingTrucks = loadService.getRemainingTrucks(testLoad.getLoadId());

        assertEquals(2, remainingTrucks); // 3 - 1 = 2
    }

    @Test
    void updateLoadStatusOnBid_FromPostedToOpenForBids() {
        when(loadRepository.findByIdWithLock(testLoad.getLoadId())).thenReturn(Optional.of(testLoad));
        when(loadRepository.save(any(Load.class))).thenReturn(testLoad);

        loadService.updateLoadStatusOnBid(testLoad.getLoadId());

        assertEquals(LoadStatus.OPEN_FOR_BIDS, testLoad.getStatus());
        verify(loadRepository, times(1)).save(testLoad);
    }

    @Test
    void updateLoadStatusOnBooking_ToBooked() {
        testLoad.setStatus(LoadStatus.OPEN_FOR_BIDS);
        when(loadRepository.findByIdWithLock(testLoad.getLoadId())).thenReturn(Optional.of(testLoad));
        when(bookingRepository.sumAllocatedTrucksByLoadId(testLoad.getLoadId())).thenReturn(3); // All trucks allocated
        when(loadRepository.save(any(Load.class))).thenReturn(testLoad);

        loadService.updateLoadStatusOnBooking(testLoad.getLoadId());

        assertEquals(LoadStatus.BOOKED, testLoad.getStatus());
        verify(loadRepository, times(1)).save(testLoad);
    }
}
