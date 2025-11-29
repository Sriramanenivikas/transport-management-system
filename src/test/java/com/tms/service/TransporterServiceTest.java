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

import java.util.*;

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
        TruckCapacity truckCapacity = TruckCapacity.builder()
                .id(UUID.randomUUID())
                .truckType("Container")
                .count(10)
                .build();

        testTransporter = Transporter.builder()
                .transporterId(UUID.randomUUID())
                .companyName("ABC Transport")
                .rating(4.5)
                .availableTrucks(new ArrayList<>(List.of(truckCapacity)))
                .build();
        truckCapacity.setTransporter(testTransporter);

        testTransporterRequest = TransporterRequest.builder()
                .companyName("ABC Transport")
                .rating(4.5)
                .availableTrucks(List.of(
                        TruckCapacityDto.builder().truckType("Container").count(10).build()
                ))
                .build();
    }

    @Test
    void createTransporter_Success() {
        when(transporterRepository.save(any(Transporter.class))).thenReturn(testTransporter);

        TransporterResponse response = transporterService.createTransporter(testTransporterRequest);

        assertNotNull(response);
        assertEquals("ABC Transport", response.getCompanyName());
        assertEquals(4.5, response.getRating());
        assertFalse(response.getAvailableTrucks().isEmpty());
        verify(transporterRepository, times(1)).save(any(Transporter.class));
    }

    @Test
    void getTransporterById_Success() {
        when(transporterRepository.findById(testTransporter.getTransporterId())).thenReturn(Optional.of(testTransporter));

        TransporterResponse response = transporterService.getTransporterById(testTransporter.getTransporterId());

        assertNotNull(response);
        assertEquals(testTransporter.getTransporterId(), response.getTransporterId());
        assertEquals("ABC Transport", response.getCompanyName());
    }

    @Test
    void getTransporterById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(transporterRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transporterService.getTransporterById(nonExistentId));
    }

    @Test
    void updateTrucks_Success() {
        UpdateTrucksRequest updateRequest = UpdateTrucksRequest.builder()
                .availableTrucks(List.of(
                        TruckCapacityDto.builder().truckType("Flatbed").count(5).build()
                ))
                .build();

        when(transporterRepository.findById(testTransporter.getTransporterId())).thenReturn(Optional.of(testTransporter));
        when(transporterRepository.save(any(Transporter.class))).thenReturn(testTransporter);

        TransporterResponse response = transporterService.updateTrucks(testTransporter.getTransporterId(), updateRequest);

        assertNotNull(response);
        verify(transporterRepository, times(1)).save(testTransporter);
    }

    @Test
    void getAvailableTrucks_Success() {
        when(transporterRepository.findById(testTransporter.getTransporterId())).thenReturn(Optional.of(testTransporter));

        int availableTrucks = transporterService.getAvailableTrucks(testTransporter.getTransporterId(), "Container");

        assertEquals(10, availableTrucks);
    }

    @Test
    void getAvailableTrucks_NoMatchingTruckType() {
        when(transporterRepository.findById(testTransporter.getTransporterId())).thenReturn(Optional.of(testTransporter));

        int availableTrucks = transporterService.getAvailableTrucks(testTransporter.getTransporterId(), "Flatbed");

        assertEquals(0, availableTrucks);
    }

    @Test
    void deductTrucks_Success() {
        when(transporterRepository.findById(testTransporter.getTransporterId())).thenReturn(Optional.of(testTransporter));

        transporterService.deductTrucks(testTransporter.getTransporterId(), "Container", 3);

        TruckCapacity truckCapacity = testTransporter.getAvailableTrucks().get(0);
        assertEquals(7, truckCapacity.getCount());
        verify(truckCapacityRepository, times(1)).save(truckCapacity);
    }

    @Test
    void deductTrucks_InsufficientCapacity() {
        when(transporterRepository.findById(testTransporter.getTransporterId())).thenReturn(Optional.of(testTransporter));

        assertThrows(InsufficientCapacityException.class, 
                () -> transporterService.deductTrucks(testTransporter.getTransporterId(), "Container", 15));
    }

    @Test
    void deductTrucks_NoMatchingTruckType() {
        when(transporterRepository.findById(testTransporter.getTransporterId())).thenReturn(Optional.of(testTransporter));

        assertThrows(InsufficientCapacityException.class, 
                () -> transporterService.deductTrucks(testTransporter.getTransporterId(), "Flatbed", 5));
    }

    @Test
    void restoreTrucks_ExistingTruckType() {
        when(transporterRepository.findById(testTransporter.getTransporterId())).thenReturn(Optional.of(testTransporter));

        transporterService.restoreTrucks(testTransporter.getTransporterId(), "Container", 5);

        TruckCapacity truckCapacity = testTransporter.getAvailableTrucks().get(0);
        assertEquals(15, truckCapacity.getCount());
        verify(truckCapacityRepository, times(1)).save(truckCapacity);
    }

    @Test
    void restoreTrucks_NewTruckType() {
        when(transporterRepository.findById(testTransporter.getTransporterId())).thenReturn(Optional.of(testTransporter));
        when(transporterRepository.save(any(Transporter.class))).thenReturn(testTransporter);

        transporterService.restoreTrucks(testTransporter.getTransporterId(), "Flatbed", 5);

        verify(transporterRepository, times(1)).save(testTransporter);
    }
}
