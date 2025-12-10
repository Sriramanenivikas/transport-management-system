package com.tms.controller;

import com.tms.dto.*;
import com.tms.service.LoadService;
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
public class LoadController {

    private final LoadService loadService;

    public LoadController(LoadService loadService) {
        this.loadService = loadService;
    }

    @PostMapping
    public ResponseEntity<LoadResponse> createLoad(@Valid @RequestBody LoadRequest request) {
        LoadResponse response = loadService.createLoad(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<LoadResponse>> getLoads(
            @RequestParam(required = false) String shipperId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LoadResponse> response = loadService.getLoads(shipperId, status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{loadId}")
    public ResponseEntity<LoadResponse> getLoadById(@PathVariable Integer loadId) {
        LoadResponse response = loadService.getLoadById(loadId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{loadId}/cancel")
    public ResponseEntity<LoadResponse> cancelLoad(@PathVariable Integer loadId) {
        LoadResponse response = loadService.cancelLoad(loadId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{loadId}/best-bids")
    public ResponseEntity<List<BidResponse>> getBestBids(@PathVariable Integer loadId) {
        List<BidResponse> response = loadService.getBestBids(loadId);
        return ResponseEntity.ok(response);
    }
}

