package com.tms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tms.dto.*;
import com.tms.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completeBookingFlow_Success() throws Exception {
        // 1. Create a transporter
        TransporterRequest transporterRequest = TransporterRequest.builder()
                .companyName("ABC Transport")
                .rating(4.5)
                .availableTrucks(List.of(
                        TruckCapacityDto.builder().truckType("Container").count(10).build()
                ))
                .build();

        MvcResult transporterResult = mockMvc.perform(post("/transporter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transporterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transporterId").exists())
                .andReturn();

        TransporterResponse transporter = objectMapper.readValue(
                transporterResult.getResponse().getContentAsString(), TransporterResponse.class);

        // 2. Create a load
        LoadRequest loadRequest = LoadRequest.builder()
                .shipperId("SHIP001")
                .loadingCity("Mumbai")
                .unloadingCity("Delhi")
                .loadingDate(LocalDateTime.now().plusDays(1))
                .productType("Electronics")
                .weight(5000.0)
                .weightUnit(WeightUnit.KG)
                .truckType("Container")
                .noOfTrucks(3)
                .build();

        MvcResult loadResult = mockMvc.perform(post("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("POSTED"))
                .andReturn();

        LoadResponse load = objectMapper.readValue(
                loadResult.getResponse().getContentAsString(), LoadResponse.class);

        // 3. Submit a bid
        BidRequest bidRequest = BidRequest.builder()
                .loadId(load.getLoadId())
                .transporterId(transporter.getTransporterId())
                .proposedRate(50000.0)
                .trucksOffered(2)
                .build();

        MvcResult bidResult = mockMvc.perform(post("/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bidRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        BidResponse bid = objectMapper.readValue(
                bidResult.getResponse().getContentAsString(), BidResponse.class);

        // Verify load status changed to OPEN_FOR_BIDS
        mockMvc.perform(get("/load/" + load.getLoadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN_FOR_BIDS"));

        // 4. Create a booking
        BookingRequest bookingRequest = BookingRequest.builder()
                .bidId(bid.getBidId())
                .build();

        MvcResult bookingResult = mockMvc.perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.allocatedTrucks").value(2))
                .andReturn();

        BookingResponse booking = objectMapper.readValue(
                bookingResult.getResponse().getContentAsString(), BookingResponse.class);

        // Verify transporter trucks were deducted
        mockMvc.perform(get("/transporter/" + transporter.getTransporterId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableTrucks[0].count").value(8));

        // Verify load still has remaining trucks
        mockMvc.perform(get("/load/" + load.getLoadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remainingTrucks").value(1))
                .andExpect(jsonPath("$.status").value("OPEN_FOR_BIDS"));

        // 5. Cancel the booking
        mockMvc.perform(patch("/booking/" + booking.getBookingId() + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Verify transporter trucks were restored
        mockMvc.perform(get("/transporter/" + transporter.getTransporterId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableTrucks[0].count").value(10));

        // Verify load remaining trucks were updated
        mockMvc.perform(get("/load/" + load.getLoadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remainingTrucks").value(3));
    }

    @Test
    void multiTruckAllocation_FullyBooked() throws Exception {
        // Create a transporter with enough trucks
        TransporterRequest transporterRequest = TransporterRequest.builder()
                .companyName("ABC Transport")
                .rating(4.5)
                .availableTrucks(List.of(
                        TruckCapacityDto.builder().truckType("Container").count(10).build()
                ))
                .build();

        MvcResult transporterResult = mockMvc.perform(post("/transporter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transporterRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        TransporterResponse transporter = objectMapper.readValue(
                transporterResult.getResponse().getContentAsString(), TransporterResponse.class);

        // Create a load requiring 3 trucks
        LoadRequest loadRequest = LoadRequest.builder()
                .shipperId("SHIP001")
                .loadingCity("Mumbai")
                .unloadingCity("Delhi")
                .loadingDate(LocalDateTime.now().plusDays(1))
                .productType("Electronics")
                .weight(5000.0)
                .weightUnit(WeightUnit.KG)
                .truckType("Container")
                .noOfTrucks(3)
                .build();

        MvcResult loadResult = mockMvc.perform(post("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        LoadResponse load = objectMapper.readValue(
                loadResult.getResponse().getContentAsString(), LoadResponse.class);

        // Submit and book first bid (2 trucks)
        BidRequest bid1Request = BidRequest.builder()
                .loadId(load.getLoadId())
                .transporterId(transporter.getTransporterId())
                .proposedRate(50000.0)
                .trucksOffered(2)
                .build();

        MvcResult bid1Result = mockMvc.perform(post("/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid1Request)))
                .andExpect(status().isCreated())
                .andReturn();

        BidResponse bid1 = objectMapper.readValue(
                bid1Result.getResponse().getContentAsString(), BidResponse.class);

        mockMvc.perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(BookingRequest.builder().bidId(bid1.getBidId()).build())))
                .andExpect(status().isCreated());

        // Verify load is still OPEN_FOR_BIDS (1 truck remaining)
        mockMvc.perform(get("/load/" + load.getLoadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remainingTrucks").value(1))
                .andExpect(jsonPath("$.status").value("OPEN_FOR_BIDS"));

        // Submit and book second bid (1 truck - completes the load)
        BidRequest bid2Request = BidRequest.builder()
                .loadId(load.getLoadId())
                .transporterId(transporter.getTransporterId())
                .proposedRate(45000.0)
                .trucksOffered(1)
                .build();

        MvcResult bid2Result = mockMvc.perform(post("/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid2Request)))
                .andExpect(status().isCreated())
                .andReturn();

        BidResponse bid2 = objectMapper.readValue(
                bid2Result.getResponse().getContentAsString(), BidResponse.class);

        mockMvc.perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(BookingRequest.builder().bidId(bid2.getBidId()).build())))
                .andExpect(status().isCreated());

        // Verify load is now BOOKED (0 trucks remaining)
        mockMvc.perform(get("/load/" + load.getLoadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remainingTrucks").value(0))
                .andExpect(jsonPath("$.status").value("BOOKED"));
    }

    @Test
    void bidOnCancelledLoad_ShouldFail() throws Exception {
        // Create transporter
        TransporterRequest transporterRequest = TransporterRequest.builder()
                .companyName("ABC Transport")
                .rating(4.5)
                .availableTrucks(List.of(
                        TruckCapacityDto.builder().truckType("Container").count(10).build()
                ))
                .build();

        MvcResult transporterResult = mockMvc.perform(post("/transporter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transporterRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        TransporterResponse transporter = objectMapper.readValue(
                transporterResult.getResponse().getContentAsString(), TransporterResponse.class);

        // Create and cancel a load
        LoadRequest loadRequest = LoadRequest.builder()
                .shipperId("SHIP001")
                .loadingCity("Mumbai")
                .unloadingCity("Delhi")
                .loadingDate(LocalDateTime.now().plusDays(1))
                .productType("Electronics")
                .weight(5000.0)
                .weightUnit(WeightUnit.KG)
                .truckType("Container")
                .noOfTrucks(3)
                .build();

        MvcResult loadResult = mockMvc.perform(post("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        LoadResponse load = objectMapper.readValue(
                loadResult.getResponse().getContentAsString(), LoadResponse.class);

        mockMvc.perform(patch("/load/" + load.getLoadId() + "/cancel"))
                .andExpect(status().isOk());

        // Try to bid on cancelled load
        BidRequest bidRequest = BidRequest.builder()
                .loadId(load.getLoadId())
                .transporterId(transporter.getTransporterId())
                .proposedRate(50000.0)
                .trucksOffered(2)
                .build();

        mockMvc.perform(post("/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("CANCELLED")));
    }

    @Test
    void bidExceedsCapacity_ShouldFail() throws Exception {
        // Create transporter with limited trucks
        TransporterRequest transporterRequest = TransporterRequest.builder()
                .companyName("ABC Transport")
                .rating(4.5)
                .availableTrucks(List.of(
                        TruckCapacityDto.builder().truckType("Container").count(2).build()
                ))
                .build();

        MvcResult transporterResult = mockMvc.perform(post("/transporter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transporterRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        TransporterResponse transporter = objectMapper.readValue(
                transporterResult.getResponse().getContentAsString(), TransporterResponse.class);

        // Create a load
        LoadRequest loadRequest = LoadRequest.builder()
                .shipperId("SHIP001")
                .loadingCity("Mumbai")
                .unloadingCity("Delhi")
                .loadingDate(LocalDateTime.now().plusDays(1))
                .productType("Electronics")
                .weight(5000.0)
                .weightUnit(WeightUnit.KG)
                .truckType("Container")
                .noOfTrucks(5)
                .build();

        MvcResult loadResult = mockMvc.perform(post("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        LoadResponse load = objectMapper.readValue(
                loadResult.getResponse().getContentAsString(), LoadResponse.class);

        // Try to bid more trucks than available
        BidRequest bidRequest = BidRequest.builder()
                .loadId(load.getLoadId())
                .transporterId(transporter.getTransporterId())
                .proposedRate(50000.0)
                .trucksOffered(5)  // More than available (2)
                .build();

        mockMvc.perform(post("/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("not have enough trucks")));
    }

    @Test
    void getBestBids_SortedByScore() throws Exception {
        // Create two transporters with different ratings
        TransporterRequest transporter1Request = TransporterRequest.builder()
                .companyName("ABC Transport")
                .rating(4.5)
                .availableTrucks(List.of(
                        TruckCapacityDto.builder().truckType("Container").count(10).build()
                ))
                .build();

        MvcResult transporter1Result = mockMvc.perform(post("/transporter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transporter1Request)))
                .andExpect(status().isCreated())
                .andReturn();

        TransporterResponse transporter1 = objectMapper.readValue(
                transporter1Result.getResponse().getContentAsString(), TransporterResponse.class);

        TransporterRequest transporter2Request = TransporterRequest.builder()
                .companyName("XYZ Logistics")
                .rating(3.0)
                .availableTrucks(List.of(
                        TruckCapacityDto.builder().truckType("Container").count(10).build()
                ))
                .build();

        MvcResult transporter2Result = mockMvc.perform(post("/transporter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transporter2Request)))
                .andExpect(status().isCreated())
                .andReturn();

        TransporterResponse transporter2 = objectMapper.readValue(
                transporter2Result.getResponse().getContentAsString(), TransporterResponse.class);

        // Create a load
        LoadRequest loadRequest = LoadRequest.builder()
                .shipperId("SHIP001")
                .loadingCity("Mumbai")
                .unloadingCity("Delhi")
                .loadingDate(LocalDateTime.now().plusDays(1))
                .productType("Electronics")
                .weight(5000.0)
                .weightUnit(WeightUnit.KG)
                .truckType("Container")
                .noOfTrucks(3)
                .build();

        MvcResult loadResult = mockMvc.perform(post("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        LoadResponse load = objectMapper.readValue(
                loadResult.getResponse().getContentAsString(), LoadResponse.class);

        // Submit bids from both transporters
        BidRequest bid1Request = BidRequest.builder()
                .loadId(load.getLoadId())
                .transporterId(transporter1.getTransporterId())
                .proposedRate(50000.0)
                .trucksOffered(2)
                .build();

        mockMvc.perform(post("/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid1Request)))
                .andExpect(status().isCreated());

        BidRequest bid2Request = BidRequest.builder()
                .loadId(load.getLoadId())
                .transporterId(transporter2.getTransporterId())
                .proposedRate(40000.0)  // Lower rate
                .trucksOffered(2)
                .build();

        mockMvc.perform(post("/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid2Request)))
                .andExpect(status().isCreated());

        // Get best bids
        mockMvc.perform(get("/load/" + load.getLoadId() + "/best-bids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].score").exists())
                .andExpect(jsonPath("$[1].score").exists());
    }
}
