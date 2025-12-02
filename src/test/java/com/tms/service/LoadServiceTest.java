package com.tms.service;

import com.tms.dto.LoadRequest;
import com.tms.dto.LoadResponse;
import com.tms.entity.Load;
import com.tms.exception.InvalidStatusTransitionException;
import com.tms.exception.ResourceNotFoundException;
import com.tms.repository.BidRepository;
import com.tms.repository.BookingRepository;
import com.tms.repository.LoadRepository;
import com.tms.repository.TransporterRepository;
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
import java.util.Collections;
import java.util.Optional;

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

    @Mock
    private TransporterRepository transporterRepository;

    @InjectMocks
    private LoadService loadService;

    private Load testLoad;
    private LoadRequest testLoadRequest;

    @BeforeEach
    void setUp() {
        testLoad = new Load();
        testLoad.setLoadId(1);
        testLoad.setShipperId("SHIP001");
        testLoad.setLoadingCity("Mumbai");
        testLoad.setUnloadingCity("Delhi");
        testLoad.setLoadingDate(LocalDateTime.now().plusDays(5));
        testLoad.setProductType("Electronics");
        testLoad.setWeight(5000.0);
        testLoad.setWeightUnit("KG");
        testLoad.setTruckType("CONTAINER-20FT");
        testLoad.setNoOfTrucks(3);
        testLoad.setStatus("POSTED");

        testLoadRequest = new LoadRequest();
        testLoadRequest.setShipperId("SHIP001");
        testLoadRequest.setLoadingCity("Mumbai");
        testLoadRequest.setUnloadingCity("Delhi");
        testLoadRequest.setLoadingDate(LocalDateTime.now().plusDays(5));
        testLoadRequest.setProductType("Electronics");
        testLoadRequest.setWeight(5000.0);
        testLoadRequest.setWeightUnit("KG");
        testLoadRequest.setTruckType("CONTAINER-20FT");
        testLoadRequest.setNoOfTrucks(3);
    }

    @Test
    void createLoad_Success() {
        when(loadRepository.save(any(Load.class))).thenReturn(testLoad);
        when(bookingRepository.getTotalAllocatedTrucks(any())).thenReturn(0);

        LoadResponse response = loadService.createLoad(testLoadRequest);

        assertNotNull(response);
        assertEquals("SHIP001", response.getShipperId());
        assertEquals("POSTED", response.getStatus());
        verify(loadRepository, times(1)).save(any(Load.class));
    }

    @Test
    void getLoadById_Success() {
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));
        when(bidRepository.findByLoadIdAndStatus(1, "PENDING")).thenReturn(Collections.emptyList());
        when(bookingRepository.getTotalAllocatedTrucks(1)).thenReturn(0);

        LoadResponse response = loadService.getLoadById(1);

        assertNotNull(response);
        assertEquals(1, response.getLoadId());
        assertEquals("Mumbai", response.getLoadingCity());
    }

    @Test
    void getLoadById_NotFound() {
        when(loadRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> loadService.getLoadById(999));
    }

    @Test
    void cancelLoad_Success() {
        testLoad.setStatus("POSTED");
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));
        when(loadRepository.save(any(Load.class))).thenReturn(testLoad);
        when(bookingRepository.getTotalAllocatedTrucks(any())).thenReturn(0);

        LoadResponse response = loadService.cancelLoad(1);

        assertEquals("CANCELLED", response.getStatus());
    }

    @Test
    void cancelLoad_AlreadyBooked_ThrowsException() {
        testLoad.setStatus("BOOKED");
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));

        assertThrows(InvalidStatusTransitionException.class, () -> loadService.cancelLoad(1));
    }

    @Test
    void getLoads_WithPagination() {
        Page<Load> loadPage = new PageImpl<>(Collections.singletonList(testLoad));
        Pageable pageable = PageRequest.of(0, 10);
        
        when(loadRepository.findAll(pageable)).thenReturn(loadPage);
        when(bookingRepository.getTotalAllocatedTrucks(any())).thenReturn(0);

        Page<LoadResponse> response = loadService.getLoads(null, null, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void getLoads_FilterByShipperId() {
        Page<Load> loadPage = new PageImpl<>(Collections.singletonList(testLoad));
        Pageable pageable = PageRequest.of(0, 10);
        
        when(loadRepository.findByShipperId("SHIP001", pageable)).thenReturn(loadPage);
        when(bookingRepository.getTotalAllocatedTrucks(any())).thenReturn(0);

        Page<LoadResponse> response = loadService.getLoads("SHIP001", null, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void getLoads_FilterByStatus() {
        Page<Load> loadPage = new PageImpl<>(Collections.singletonList(testLoad));
        Pageable pageable = PageRequest.of(0, 10);
        
        when(loadRepository.findByStatus("POSTED", pageable)).thenReturn(loadPage);
        when(bookingRepository.getTotalAllocatedTrucks(any())).thenReturn(0);

        Page<LoadResponse> response = loadService.getLoads(null, "POSTED", pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void remainingTrucks_CalculatedCorrectly() {
        testLoad.setNoOfTrucks(5);
        when(loadRepository.findById(1)).thenReturn(Optional.of(testLoad));
        when(bidRepository.findByLoadIdAndStatus(1, "PENDING")).thenReturn(Collections.emptyList());
        when(bookingRepository.getTotalAllocatedTrucks(1)).thenReturn(2);

        LoadResponse response = loadService.getLoadById(1);

        assertEquals(3, response.getRemainingTrucks());
    }
}
