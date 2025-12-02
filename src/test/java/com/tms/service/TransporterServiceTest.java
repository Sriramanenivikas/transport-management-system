package com.tms.service;

import com.tms.dto.TransporterRequest;
import com.tms.dto.TransporterResponse;
import com.tms.entity.Transporter;
import com.tms.entity.TruckCapacity;
import com.tms.exception.ResourceNotFoundException;
import com.tms.repository.TransporterRepository;
import com.tms.repository.TruckCapacityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransporterServiceTest {

    @Mock
    private TransporterRepository transporterRepository;

    @Mock
    private TruckCapacityRepository truckCapacityRepository;

    @InjectMocks
    private TransporterService transporterService;

    private Transporter testTransporter;
    private TransporterRequest testTransporterRequest;

    @BeforeEach
    void setUp() {
        testTransporter = new Transporter();
        testTransporter.setTransporterId(1);
        testTransporter.setCompanyName("Test Transport");
        testTransporter.setRating(4.5);

        TruckCapacity tc1 = new TruckCapacity("CONTAINER-20FT", 10);
        tc1.setTransporter(testTransporter);
        TruckCapacity tc2 = new TruckCapacity("CONTAINER-40FT", 5);
        tc2.setTransporter(testTransporter);

        testTransporter.setAvailableTrucks(new ArrayList<>(Arrays.asList(tc1, tc2)));

        testTransporterRequest = new TransporterRequest();
        testTransporterRequest.setCompanyName("Test Transport");
        testTransporterRequest.setRating(4.5);

        TransporterRequest.TruckCapacityDTO dto1 = new TransporterRequest.TruckCapacityDTO();
        dto1.setTruckType("CONTAINER-20FT");
        dto1.setCount(10);

        TransporterRequest.TruckCapacityDTO dto2 = new TransporterRequest.TruckCapacityDTO();
        dto2.setTruckType("CONTAINER-40FT");
        dto2.setCount(5);

        testTransporterRequest.setAvailableTrucks(Arrays.asList(dto1, dto2));
    }

    @Test
    void createTransporter_Success() {
        when(transporterRepository.save(any(Transporter.class))).thenReturn(testTransporter);

        TransporterResponse response = transporterService.createTransporter(testTransporterRequest);

        assertNotNull(response);
        assertEquals("Test Transport", response.getCompanyName());
        assertEquals(4.5, response.getRating());
        assertEquals(2, response.getAvailableTrucks().size());
    }

    @Test
    void getTransporterById_Success() {
        when(transporterRepository.findById(1)).thenReturn(Optional.of(testTransporter));

        TransporterResponse response = transporterService.getTransporterById(1);

        assertNotNull(response);
        assertEquals(1, response.getTransporterId());
        assertEquals("Test Transport", response.getCompanyName());
    }

    @Test
    void getTransporterById_NotFound_ThrowsException() {
        when(transporterRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transporterService.getTransporterById(999));
    }

    @Test
    void getAllTransporters_Success() {
        when(transporterRepository.findAll()).thenReturn(Arrays.asList(testTransporter));

        List<TransporterResponse> response = transporterService.getAllTransporters();

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void updateTruckCapacity_Success() {
        TransporterRequest.TruckCapacityDTO dto = new TransporterRequest.TruckCapacityDTO();
        dto.setTruckType("FLATBED-TRAILER");
        dto.setCount(15);

        when(transporterRepository.findById(1)).thenReturn(Optional.of(testTransporter));
        when(transporterRepository.save(any(Transporter.class))).thenReturn(testTransporter);
        doNothing().when(truckCapacityRepository).deleteByTransporterTransporterId(1);

        TransporterResponse response = transporterService.updateTruckCapacity(1, Arrays.asList(dto));

        assertNotNull(response);
        verify(truckCapacityRepository).deleteByTransporterTransporterId(1);
        verify(transporterRepository).save(any(Transporter.class));
    }

    @Test
    void updateTruckCapacity_TransporterNotFound_ThrowsException() {
        when(transporterRepository.findById(999)).thenReturn(Optional.empty());
        doNothing().when(truckCapacityRepository).deleteByTransporterTransporterId(999);

        TransporterRequest.TruckCapacityDTO dto = new TransporterRequest.TruckCapacityDTO();
        dto.setTruckType("FLATBED-TRAILER");
        dto.setCount(15);

        assertThrows(ResourceNotFoundException.class, 
            () -> transporterService.updateTruckCapacity(999, Arrays.asList(dto)));
    }
}
