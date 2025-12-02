package com.tms.controller;

import com.tms.dto.*;
import com.tms.service.LoadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/load")
@Tag(name = "Load", description = "Load management APIs")
public class LoadController {

    private final LoadService loadService;

    public LoadController(LoadService loadService) {
        this.loadService = loadService;
    }

    @PostMapping
    @Operation(summary = "Create a new load", description = "Creates a new load with status POSTED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Load created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<LoadResponse> createLoad(@Valid @RequestBody LoadRequest request) {
        LoadResponse response = loadService.createLoad(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get loads with pagination", description = "Returns paginated list of loads with optional filters")
    public ResponseEntity<Page<LoadResponse>> getLoads(
            @Parameter(description = "Filter by shipper ID") @RequestParam(required = false) String shipperId,
            @Parameter(description = "Filter by status (POSTED, OPEN_FOR_BIDS, BOOKED, CANCELLED)") @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LoadResponse> response = loadService.getLoads(shipperId, status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{loadId}")
    @Operation(summary = "Get load by ID", description = "Returns load details with active bids")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Load found"),
            @ApiResponse(responseCode = "404", description = "Load not found")
    })
    public ResponseEntity<LoadResponse> getLoadById(@PathVariable Integer loadId) {
        LoadResponse response = loadService.getLoadById(loadId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{loadId}/cancel")
    @Operation(summary = "Cancel a load", description = "Cancels a load (cannot cancel if already BOOKED)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Load cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot cancel booked load"),
            @ApiResponse(responseCode = "404", description = "Load not found")
    })
    public ResponseEntity<LoadResponse> cancelLoad(@PathVariable Integer loadId) {
        LoadResponse response = loadService.cancelLoad(loadId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{loadId}/best-bids")
    @Operation(summary = "Get best bids for a load", description = "Returns bids sorted by score: (1/rate)*0.7 + (rating/5)*0.3")
    public ResponseEntity<List<BidResponse>> getBestBids(@PathVariable Integer loadId) {
        List<BidResponse> response = loadService.getBestBids(loadId);
        return ResponseEntity.ok(response);
    }
}

