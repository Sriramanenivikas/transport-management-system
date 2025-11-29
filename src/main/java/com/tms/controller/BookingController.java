package com.tms.controller;

import com.tms.dto.*;
import com.tms.service.BookingService;
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
@RequestMapping("/booking")
@RequiredArgsConstructor
@Tag(name = "Booking", description = "Booking management APIs")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a booking", description = "Accept a bid and create a booking (handles concurrency)")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking by ID", description = "Get booking details")
    public ResponseEntity<BookingResponse> getBookingById(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {
        BookingResponse response = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{bookingId}/cancel")
    @Operation(summary = "Cancel a booking", description = "Cancel a booking (restores trucks, updates load status)")
    public ResponseEntity<BookingResponse> cancelBooking(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {
        BookingResponse response = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(response);
    }
}
