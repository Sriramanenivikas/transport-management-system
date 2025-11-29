package com.tms.controller;

import com.tms.dto.*;
import com.tms.entity.LoadStatus;
import com.tms.service.LoadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/load")
@RequiredArgsConstructor
@Tag(name = "Load", description = "Load management APIs")
public class LoadController {

    private final LoadService loadService;

    @PostMapping
    @Operation(summary = "Create a new load", description = "Creates a new load with status POSTED")
    public ResponseEntity<LoadResponse> createLoad(@Valid @RequestBody LoadRequest request) {
        LoadResponse response = loadService.createLoad(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get loads with pagination", description = "List loads with optional filters for shipperId and status")
    public ResponseEntity<Page<LoadResponse>> getLoads(
            @Parameter(description = "Filter by shipper ID") @RequestParam(required = false) String shipperId,
            @Parameter(description = "Filter by load status") @RequestParam(required = false) LoadStatus status,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LoadResponse> response = loadService.getLoads(shipperId, status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{loadId}")
    @Operation(summary = "Get load by ID", description = "Get load details with active bids")
    public ResponseEntity<LoadResponse> getLoadById(
            @Parameter(description = "Load ID") @PathVariable UUID loadId) {
        LoadResponse response = loadService.getLoadById(loadId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{loadId}/cancel")
    @Operation(summary = "Cancel a load", description = "Cancel a load (validates status transition)")
    public ResponseEntity<LoadResponse> cancelLoad(
            @Parameter(description = "Load ID") @PathVariable UUID loadId) {
        LoadResponse response = loadService.cancelLoad(loadId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{loadId}/best-bids")
    @Operation(summary = "Get best bids for a load", description = "Get sorted bid suggestions based on score calculation")
    public ResponseEntity<List<BidResponse>> getBestBids(
            @Parameter(description = "Load ID") @PathVariable UUID loadId) {
        List<BidResponse> response = loadService.getBestBids(loadId);
        return ResponseEntity.ok(response);
    }
}
