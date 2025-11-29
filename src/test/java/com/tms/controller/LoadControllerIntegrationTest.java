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
class LoadControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createLoad_Success() throws Exception {
        LoadRequest request = LoadRequest.builder()
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

        mockMvc.perform(post("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loadId").exists())
                .andExpect(jsonPath("$.shipperId").value("SHIP001"))
                .andExpect(jsonPath("$.status").value("POSTED"))
                .andExpect(jsonPath("$.remainingTrucks").value(3));
    }

    @Test
    void createLoad_ValidationError() throws Exception {
        LoadRequest request = LoadRequest.builder()
                .shipperId("")  // Invalid: empty
                .loadingCity("Mumbai")
                .unloadingCity("Delhi")
                .loadingDate(LocalDateTime.now().plusDays(1))
                .productType("Electronics")
                .weight(-5000.0)  // Invalid: negative
                .weightUnit(WeightUnit.KG)
                .truckType("Container")
                .noOfTrucks(0)  // Invalid: zero
                .build();

        mockMvc.perform(post("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    void getLoads_WithPagination() throws Exception {
        // Create a load first
        LoadRequest request = LoadRequest.builder()
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

        mockMvc.perform(post("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Get loads with pagination
        mockMvc.perform(get("/load")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getLoadById_Success() throws Exception {
        // Create a load first
        LoadRequest request = LoadRequest.builder()
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

        MvcResult result = mockMvc.perform(post("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        LoadResponse createdLoad = objectMapper.readValue(
                result.getResponse().getContentAsString(), LoadResponse.class);

        // Get load by ID
        mockMvc.perform(get("/load/" + createdLoad.getLoadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loadId").value(createdLoad.getLoadId().toString()))
                .andExpect(jsonPath("$.shipperId").value("SHIP001"));
    }

    @Test
    void getLoadById_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/load/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    void cancelLoad_Success() throws Exception {
        // Create a load first
        LoadRequest request = LoadRequest.builder()
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

        MvcResult result = mockMvc.perform(post("/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        LoadResponse createdLoad = objectMapper.readValue(
                result.getResponse().getContentAsString(), LoadResponse.class);

        // Cancel load
        mockMvc.perform(patch("/load/" + createdLoad.getLoadId() + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
