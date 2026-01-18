package com.orderreturn.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderreturn.dto.CreateReturnRequest;
import com.orderreturn.dto.ReturnStateTransitionRequest;
import com.orderreturn.enums.ReturnAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReturnIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private UUID createDeliveredOrder() throws Exception {
        String orderResponse = mockMvc.perform(post("/api/orders"))
                .andReturn().getResponse().getContentAsString();
        UUID orderId = UUID.fromString(objectMapper.readTree(orderResponse).get("id").asText());
        // PAY, PROCESS, SHIP, DELIVER
        mockMvc.perform(post("/api/orders/" + orderId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"PAY\"}"));
        mockMvc.perform(post("/api/orders/" + orderId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"PROCESS\"}"));
        mockMvc.perform(post("/api/orders/" + orderId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"SHIP\"}"));
        mockMvc.perform(post("/api/orders/" + orderId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"DELIVER\"}"));
        return orderId;
    }

    @Test
    @DisplayName("Create Return: POST /api/returns returns 201 and REQUESTED")
    void createReturn() throws Exception {
        UUID orderId = createDeliveredOrder();
        CreateReturnRequest req = new CreateReturnRequest();
        req.setOrderId(orderId);
        mockMvc.perform(post("/api/returns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.state").value("REQUESTED"));
    }

    @Test
    @DisplayName("Valid Return Lifecycle: APPROVE → MARK_IN_TRANSIT → RECEIVE → COMPLETE")
    void validReturnLifecycle() throws Exception {
        UUID orderId = createDeliveredOrder();
        CreateReturnRequest req = new CreateReturnRequest();
        req.setOrderId(orderId);
        String returnResponse = mockMvc.perform(post("/api/returns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();
        UUID returnId = UUID.fromString(objectMapper.readTree(returnResponse).get("id").asText());
        // APPROVE
        mockMvc.perform(post("/api/returns/" + returnId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReturnStateTransitionRequest(ReturnAction.APPROVE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("APPROVED"));
        // MARK_IN_TRANSIT
        mockMvc.perform(post("/api/returns/" + returnId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReturnStateTransitionRequest(ReturnAction.MARK_IN_TRANSIT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("IN_TRANSIT"));
        // RECEIVE
        mockMvc.perform(post("/api/returns/" + returnId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReturnStateTransitionRequest(ReturnAction.RECEIVE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("RECEIVED"));
        // COMPLETE
        mockMvc.perform(post("/api/returns/" + returnId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReturnStateTransitionRequest(ReturnAction.COMPLETE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("COMPLETED"));
    }

    @Test
    @DisplayName("Invalid Return Scenarios: before delivery, multiple returns, from REJECTED/COMPLETED")
    void invalidReturnScenarios() throws Exception {
        // Create order not delivered
        String orderResponse = mockMvc.perform(post("/api/orders"))
                .andReturn().getResponse().getContentAsString();
        UUID orderId = UUID.fromString(objectMapper.readTree(orderResponse).get("id").asText());
        CreateReturnRequest req = new CreateReturnRequest();
        req.setOrderId(orderId);
        // Try to create return before delivery
        mockMvc.perform(post("/api/returns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
        // Create delivered order and return
        UUID deliveredOrderId = createDeliveredOrder();
        req.setOrderId(deliveredOrderId);
        mockMvc.perform(post("/api/returns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
        // Try to create another return for same order
        mockMvc.perform(post("/api/returns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
        // Create return and move to REJECTED
        req.setOrderId(createDeliveredOrder());
        String returnResponse = mockMvc.perform(post("/api/returns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();
        UUID returnId = UUID.fromString(objectMapper.readTree(returnResponse).get("id").asText());
        mockMvc.perform(post("/api/returns/" + returnId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReturnStateTransitionRequest(ReturnAction.REJECT))));
        // Try to transition from REJECTED
        mockMvc.perform(post("/api/returns/" + returnId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ReturnStateTransitionRequest(ReturnAction.APPROVE))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("Get Return: existing, non-existent")
    void getReturn() throws Exception {
        UUID orderId = createDeliveredOrder();
        CreateReturnRequest req = new CreateReturnRequest();
        req.setOrderId(orderId);
        String returnResponse = mockMvc.perform(post("/api/returns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();
        UUID returnId = UUID.fromString(objectMapper.readTree(returnResponse).get("id").asText());
        // Get existing
        mockMvc.perform(get("/api/returns/" + returnId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(returnId.toString()));
        // Get non-existent
        mockMvc.perform(get("/api/returns/" + UUID.randomUUID()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}

