package com.tms.controller;

import com.tms.dto.*;
import com.tms.entity.BidStatus;
import com.tms.service.BidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bid")
@RequiredArgsConstructor
@Tag(name = "Bid", description = "Bid management APIs")
public class BidController {

    private final BidService bidService;

    @PostMapping
    @Operation(summary = "Submit a bid", description = "Submit a bid for a load (validates capacity and load status)")
    public ResponseEntity<BidResponse> submitBid(@Valid @RequestBody BidRequest request) {
        BidResponse response = bidService.submitBid(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get bids with filters", description = "Filter bids by loadId, transporterId, and status")
    public ResponseEntity<List<BidResponse>> getBids(
            @Parameter(description = "Filter by load ID") @RequestParam(required = false) UUID loadId,
            @Parameter(description = "Filter by transporter ID") @RequestParam(required = false) UUID transporterId,
            @Parameter(description = "Filter by bid status") @RequestParam(required = false) BidStatus status) {
        List<BidResponse> response = bidService.getBids(loadId, transporterId, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bidId}")
    @Operation(summary = "Get bid by ID", description = "Get bid details")
    public ResponseEntity<BidResponse> getBidById(
            @Parameter(description = "Bid ID") @PathVariable UUID bidId) {
        BidResponse response = bidService.getBidById(bidId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{bidId}/reject")
    @Operation(summary = "Reject a bid", description = "Reject a pending bid")
    public ResponseEntity<BidResponse> rejectBid(
            @Parameter(description = "Bid ID") @PathVariable UUID bidId) {
        BidResponse response = bidService.rejectBid(bidId);
        return ResponseEntity.ok(response);
    }
}
