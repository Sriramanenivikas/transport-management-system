package com.tms.service;

import com.tms.dto.TransporterRequest;
import com.tms.dto.TransporterResponse;
import com.tms.entity.Transporter;
import com.tms.entity.TruckCapacity;
import com.tms.exception.ResourceNotFoundException;
import com.tms.repository.TransporterRepository;
import com.tms.repository.TruckCapacityRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransporterService {

    private final TransporterRepository transporterRepository;
    private final TruckCapacityRepository truckCapacityRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public TransporterService(TransporterRepository transporterRepository,
                             TruckCapacityRepository truckCapacityRepository) {
        this.transporterRepository = transporterRepository;
        this.truckCapacityRepository = truckCapacityRepository;
    }

    @Transactional
    public TransporterResponse createTransporter(TransporterRequest request) {
        Transporter transporter = new Transporter();
        transporter.setCompanyName(request.getCompanyName());
        transporter.setRating(request.getRating());

        List<TruckCapacity> trucks = request.getAvailableTrucks().stream()
                .map(dto -> new TruckCapacity(dto.getTruckType(), dto.getCount()))
                .collect(Collectors.toList());

        transporter.setAvailableTrucks(trucks);
        transporter = transporterRepository.save(transporter);

        return toTransporterResponse(transporter);
    }

    public TransporterResponse getTransporterById(Integer transporterId) {
        Transporter transporter = transporterRepository.findById(transporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Transporter not found with ID: " + transporterId));
        return toTransporterResponse(transporter);
    }

    public List<TransporterResponse> getAllTransporters() {
        return transporterRepository.findAll().stream()
                .map(this::toTransporterResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransporterResponse updateTruckCapacity(Integer transporterId, List<TransporterRequest.TruckCapacityDTO> trucks) {
        truckCapacityRepository.deleteByTransporterTransporterId(transporterId);
        truckCapacityRepository.flush();

        Transporter transporter = transporterRepository.findById(transporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Transporter not found with ID: " + transporterId));

        for (TransporterRequest.TruckCapacityDTO dto : trucks) {
            TruckCapacity tc = new TruckCapacity(dto.getTruckType(), dto.getCount());
            tc.setTransporter(transporter);
            transporter.getAvailableTrucks().add(tc);
        }

        transporter = transporterRepository.save(transporter);
        return toTransporterResponse(transporter);
    }

    private TransporterResponse toTransporterResponse(Transporter transporter) {
        TransporterResponse response = new TransporterResponse();
        response.setTransporterId(transporter.getTransporterId());
        response.setCompanyName(transporter.getCompanyName());
        response.setRating(transporter.getRating());

        List<TransporterResponse.TruckInfo> trucks = transporter.getAvailableTrucks().stream()
                .map(tc -> new TransporterResponse.TruckInfo(tc.getTruckType(), tc.getCount()))
                .collect(Collectors.toList());

        response.setAvailableTrucks(trucks);
        return response;
    }
}

