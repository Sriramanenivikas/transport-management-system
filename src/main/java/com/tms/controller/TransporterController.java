package com.tms.controller;

import com.tms.dto.TransporterRequest;
import com.tms.dto.TransporterResponse;
import com.tms.service.TransporterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transporter")
public class TransporterController {

    private final TransporterService transporterService;

    public TransporterController(TransporterService transporterService) {
        this.transporterService = transporterService;
    }

    @PostMapping
    public ResponseEntity<TransporterResponse> createTransporter(@Valid @RequestBody TransporterRequest request) {
        TransporterResponse response = transporterService.createTransporter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{transporterId}")
    public ResponseEntity<TransporterResponse> getTransporterById(@PathVariable Integer transporterId) {
        TransporterResponse response = transporterService.getTransporterById(transporterId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TransporterResponse>> getAllTransporters() {
        List<TransporterResponse> response = transporterService.getAllTransporters();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{transporterId}/trucks")
    public ResponseEntity<TransporterResponse> updateTruckCapacity(
            @PathVariable Integer transporterId,
            @Valid @RequestBody List<TransporterRequest.TruckCapacityDTO> trucks) {
        TransporterResponse response = transporterService.updateTruckCapacity(transporterId, trucks);
        return ResponseEntity.ok(response);
    }
}

