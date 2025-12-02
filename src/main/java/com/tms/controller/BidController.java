package com.tms.controller;

import com.tms.dto.BidRequest;
import com.tms.dto.BidResponse;
import com.tms.service.BidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bid")
@Tag(name = "Bid", description = "Bid management APIs")
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    @PostMapping
    @Operation(summary = "Submit a bid", description = "Creates a new bid (validates capacity and load status)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bid created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid bid - capacity or status issue"),
            @ApiResponse(responseCode = "404", description = "Load or transporter not found")
    })
    public ResponseEntity<BidResponse> createBid(@Valid @RequestBody BidRequest request) {
        BidResponse response = bidService.createBid(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get bids with filters", description = "Returns filtered list of bids")
    public ResponseEntity<List<BidResponse>> getBids(
            @Parameter(description = "Filter by load ID") @RequestParam(required = false) Integer loadId,
            @Parameter(description = "Filter by transporter ID") @RequestParam(required = false) Integer transporterId,
            @Parameter(description = "Filter by status (PENDING, ACCEPTED, REJECTED)") @RequestParam(required = false) String status) {
        List<BidResponse> response = bidService.getBids(loadId, transporterId, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bidId}")
    @Operation(summary = "Get bid by ID", description = "Returns bid details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bid found"),
            @ApiResponse(responseCode = "404", description = "Bid not found")
    })
    public ResponseEntity<BidResponse> getBidById(@PathVariable Integer bidId) {
        BidResponse response = bidService.getBidById(bidId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{bidId}/reject")
    @Operation(summary = "Reject a bid", description = "Rejects a pending bid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bid rejected successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot reject bid with current status"),
            @ApiResponse(responseCode = "404", description = "Bid not found")
    })
    public ResponseEntity<BidResponse> rejectBid(@PathVariable Integer bidId) {
        BidResponse response = bidService.rejectBid(bidId);
        return ResponseEntity.ok(response);
    }
}

