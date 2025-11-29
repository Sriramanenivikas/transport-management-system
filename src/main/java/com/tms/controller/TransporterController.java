package com.tms.controller;

import com.tms.dto.*;
import com.tms.service.TransporterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transporter")
@RequiredArgsConstructor
@Tag(name = "Transporter", description = "Transporter management APIs")
public class TransporterController {

    private final TransporterService transporterService;

    @PostMapping
    @Operation(summary = "Register a transporter", description = "Register a new transporter with truck capacity")
    public ResponseEntity<TransporterResponse> createTransporter(@Valid @RequestBody TransporterRequest request) {
        TransporterResponse response = transporterService.createTransporter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{transporterId}")
    @Operation(summary = "Get transporter by ID", description = "Get transporter details including available trucks")
    public ResponseEntity<TransporterResponse> getTransporterById(
            @Parameter(description = "Transporter ID") @PathVariable UUID transporterId) {
        TransporterResponse response = transporterService.getTransporterById(transporterId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{transporterId}/trucks")
    @Operation(summary = "Update transporter trucks", description = "Update available trucks for a transporter")
    public ResponseEntity<TransporterResponse> updateTrucks(
            @Parameter(description = "Transporter ID") @PathVariable UUID transporterId,
            @Valid @RequestBody UpdateTrucksRequest request) {
        TransporterResponse response = transporterService.updateTrucks(transporterId, request);
        return ResponseEntity.ok(response);
    }
}
