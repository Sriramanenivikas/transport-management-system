package com.tms.service;

import com.tms.dto.*;
import com.tms.entity.*;
import com.tms.exception.*;
import com.tms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransporterService {

    private final TransporterRepository transporterRepository;
    private final TruckCapacityRepository truckCapacityRepository;

    @Transactional
    public TransporterResponse createTransporter(TransporterRequest request) {
        Transporter transporter = Transporter.builder()
                .companyName(request.getCompanyName())
                .rating(request.getRating())
                .build();

        request.getAvailableTrucks().forEach(truckDto -> {
            TruckCapacity truckCapacity = TruckCapacity.builder()
                    .truckType(truckDto.getTruckType())
                    .count(truckDto.getCount())
                    .build();
            transporter.addTruckCapacity(truckCapacity);
        });

        Transporter savedTransporter = transporterRepository.save(transporter);
        return mapToResponse(savedTransporter);
    }

    public TransporterResponse getTransporterById(UUID transporterId) {
        Transporter transporter = transporterRepository.findById(transporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Transporter not found with id: " + transporterId));
        return mapToResponse(transporter);
    }

    @Transactional
    public TransporterResponse updateTrucks(UUID transporterId, UpdateTrucksRequest request) {
        Transporter transporter = transporterRepository.findById(transporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Transporter not found with id: " + transporterId));

        // Clear existing trucks and add new ones
        transporter.getAvailableTrucks().clear();

        request.getAvailableTrucks().forEach(truckDto -> {
            TruckCapacity truckCapacity = TruckCapacity.builder()
                    .truckType(truckDto.getTruckType())
                    .count(truckDto.getCount())
                    .build();
            transporter.addTruckCapacity(truckCapacity);
        });

        Transporter savedTransporter = transporterRepository.save(transporter);
        return mapToResponse(savedTransporter);
    }

    public int getAvailableTrucks(UUID transporterId, String truckType) {
        Transporter transporter = transporterRepository.findById(transporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Transporter not found with id: " + transporterId));

        return transporter.getAvailableTrucks().stream()
                .filter(tc -> tc.getTruckType().equalsIgnoreCase(truckType))
                .findFirst()
                .map(TruckCapacity::getCount)
                .orElse(0);
    }

    @Transactional
    public void deductTrucks(UUID transporterId, String truckType, int count) {
        Transporter transporter = transporterRepository.findById(transporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Transporter not found with id: " + transporterId));

        TruckCapacity truckCapacity = transporter.getAvailableTrucks().stream()
                .filter(tc -> tc.getTruckType().equalsIgnoreCase(truckType))
                .findFirst()
                .orElseThrow(() -> new InsufficientCapacityException("Transporter does not have trucks of type: " + truckType));

        if (truckCapacity.getCount() < count) {
            throw new InsufficientCapacityException("Insufficient trucks available. Required: " + count + ", Available: " + truckCapacity.getCount());
        }

        truckCapacity.setCount(truckCapacity.getCount() - count);
        truckCapacityRepository.save(truckCapacity);
    }

    @Transactional
    public void restoreTrucks(UUID transporterId, String truckType, int count) {
        Transporter transporter = transporterRepository.findById(transporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Transporter not found with id: " + transporterId));

        TruckCapacity truckCapacity = transporter.getAvailableTrucks().stream()
                .filter(tc -> tc.getTruckType().equalsIgnoreCase(truckType))
                .findFirst()
                .orElse(null);

        if (truckCapacity != null) {
            truckCapacity.setCount(truckCapacity.getCount() + count);
            truckCapacityRepository.save(truckCapacity);
        } else {
            // Create new truck capacity entry
            TruckCapacity newCapacity = TruckCapacity.builder()
                    .truckType(truckType)
                    .count(count)
                    .build();
            transporter.addTruckCapacity(newCapacity);
            transporterRepository.save(transporter);
        }
    }

    private TransporterResponse mapToResponse(Transporter transporter) {
        List<TruckCapacityDto> truckCapacities = transporter.getAvailableTrucks().stream()
                .map(tc -> TruckCapacityDto.builder()
                        .truckType(tc.getTruckType())
                        .count(tc.getCount())
                        .build())
                .collect(Collectors.toList());

        return TransporterResponse.builder()
                .transporterId(transporter.getTransporterId())
                .companyName(transporter.getCompanyName())
                .rating(transporter.getRating())
                .availableTrucks(truckCapacities)
                .build();
    }
}
