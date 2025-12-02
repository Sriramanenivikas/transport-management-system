package com.tms.controller;

import com.tms.dto.TransporterRequest;
import com.tms.dto.TransporterResponse;
import com.tms.service.TransporterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transporter")
@Tag(name = "Transporter", description = "Transporter management APIs")
public class TransporterController {

    private final TransporterService transporterService;

    public TransporterController(TransporterService transporterService) {
        this.transporterService = transporterService;
    }

    @PostMapping
    @Operation(summary = "Register a new transporter", description = "Creates a new transporter with truck capacity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transporter created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<TransporterResponse> createTransporter(@Valid @RequestBody TransporterRequest request) {
        TransporterResponse response = transporterService.createTransporter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{transporterId}")
    @Operation(summary = "Get transporter by ID", description = "Returns transporter details with truck capacity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transporter found"),
            @ApiResponse(responseCode = "404", description = "Transporter not found")
    })
    public ResponseEntity<TransporterResponse> getTransporterById(@PathVariable Integer transporterId) {
        TransporterResponse response = transporterService.getTransporterById(transporterId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all transporters", description = "Returns list of all transporters")
    public ResponseEntity<List<TransporterResponse>> getAllTransporters() {
        List<TransporterResponse> response = transporterService.getAllTransporters();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{transporterId}/trucks")
    @Operation(summary = "Update truck capacity", description = "Updates available trucks for a transporter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Truck capacity updated"),
            @ApiResponse(responseCode = "404", description = "Transporter not found")
    })
    public ResponseEntity<TransporterResponse> updateTruckCapacity(
            @PathVariable Integer transporterId,
            @Valid @RequestBody List<TransporterRequest.TruckCapacityDTO> trucks) {
        TransporterResponse response = transporterService.updateTruckCapacity(transporterId, trucks);
        return ResponseEntity.ok(response);
    }
}

