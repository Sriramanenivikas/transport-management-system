package com.tms.controller;

import com.tms.dto.BidRequest;
import com.tms.dto.BidResponse;
import com.tms.service.BidService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bid")
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    @PostMapping
    public ResponseEntity<BidResponse> createBid(@Valid @RequestBody BidRequest request) {
        BidResponse response = bidService.createBid(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BidResponse>> getBids(
            @RequestParam(required = false) Integer loadId,
            @RequestParam(required = false) Integer transporterId,
            @RequestParam(required = false) String status) {
        List<BidResponse> response = bidService.getBids(loadId, transporterId, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bidId}")
    public ResponseEntity<BidResponse> getBidById(@PathVariable Integer bidId) {
        BidResponse response = bidService.getBidById(bidId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{bidId}/reject")
    public ResponseEntity<BidResponse> rejectBid(@PathVariable Integer bidId) {
        BidResponse response = bidService.rejectBid(bidId);
        return ResponseEntity.ok(response);
    }
}

