package com.orderreturn.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderreturn.dto.OrderStateTransitionRequest;
import com.orderreturn.enums.OrderAction;
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
class OrderIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @DisplayName("Create Order: POST /api/orders returns 201 and PENDING_PAYMENT")
    void createOrder() throws Exception {
        mockMvc.perform(post("/api/orders"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.state").value("PENDING_PAYMENT"));
    }

    @Test
    @DisplayName("Valid Order Lifecycle: PAY → PROCESS → SHIP → DELIVER")
    void validOrderLifecycle() throws Exception {
        // Create order
        String orderResponse = mockMvc.perform(post("/api/orders"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID orderId = UUID.fromString(objectMapper.readTree(orderResponse).get("id").asText());

        // PAY
        mockMvc.perform(post("/api/orders/" + orderId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new OrderStateTransitionRequest(OrderAction.PAY))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PAID"));
        // PROCESS
        mockMvc.perform(post("/api/orders/" + orderId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new OrderStateTransitionRequest(OrderAction.PROCESS))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PROCESSING_IN_WAREHOUSE"));
        // SHIP
        mockMvc.perform(post("/api/orders/" + orderId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new OrderStateTransitionRequest(OrderAction.SHIP))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("SHIPPED"));
        // DELIVER
        mockMvc.perform(post("/api/orders/" + orderId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new OrderStateTransitionRequest(OrderAction.DELIVER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("DELIVERED"));
    }

    @Test
    @DisplayName("Invalid Order Transitions: cancel after processing, from CANCELLED, invalid action")
    void invalidOrderTransitions() throws Exception {
        // Create order and move to PROCESSING_IN_WAREHOUSE
        String orderResponse = mockMvc.perform(post("/api/orders"))
                .andReturn().getResponse().getContentAsString();
        UUID orderId = UUID.fromString(objectMapper.readTree(orderResponse).get("id").asText());
        mockMvc.perform(post("/api/orders/" + orderId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new OrderStateTransitionRequest(OrderAction.PAY))));
        mockMvc.perform(post("/api/orders/" + orderId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new OrderStateTransitionRequest(OrderAction.PROCESS))));
        // Try to cancel after processing
        mockMvc.perform(post("/api/orders/" + orderId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new OrderStateTransitionRequest(OrderAction.CANCEL))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
        // Move to CANCELLED
        String order2 = mockMvc.perform(post("/api/orders"))
                .andReturn().getResponse().getContentAsString();
        UUID orderId2 = UUID.fromString(objectMapper.readTree(order2).get("id").asText());
        mockMvc.perform(post("/api/orders/" + orderId2 + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new OrderStateTransitionRequest(OrderAction.CANCEL))));
        // Try to transition from CANCELLED
        mockMvc.perform(post("/api/orders/" + orderId2 + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new OrderStateTransitionRequest(OrderAction.PAY))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
        // Invalid action (null)
        mockMvc.perform(post("/api/orders/" + orderId2 + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("Get Order: existing, cancelled, non-existent")
    void getOrder() throws Exception {
        // Create order
        String orderResponse = mockMvc.perform(post("/api/orders"))
                .andReturn().getResponse().getContentAsString();
        UUID orderId = UUID.fromString(objectMapper.readTree(orderResponse).get("id").asText());
        // Get existing
        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()));
        // Cancel order
        mockMvc.perform(post("/api/orders/" + orderId + "/transition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new OrderStateTransitionRequest(OrderAction.CANCEL))));
        // Get cancelled (should be 400)
        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
        // Get non-existent
        mockMvc.perform(get("/api/orders/" + UUID.randomUUID()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
