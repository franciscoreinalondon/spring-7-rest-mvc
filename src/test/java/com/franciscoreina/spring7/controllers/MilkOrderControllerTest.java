package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.dto.request.order.MilkOrderRequest;
import com.franciscoreina.spring7.dto.request.order.OrderLineCreateRequest;
import com.franciscoreina.spring7.dto.request.order.OrderLineUpdateRequest;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import com.franciscoreina.spring7.dto.response.order.OrderLineResponse;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.services.MilkOrderService;
import org.instancio.Instancio;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ImportAutoConfiguration(exclude = OAuth2ResourceServerAutoConfiguration.class)
@WebMvcTest(MilkOrderController.class)
class MilkOrderControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    MilkOrderService milkOrderService;

    // ---------------
    //    POSITIVE
    // ---------------

    @Nested
    class PositiveTests {

        @Nested
        class MilkOrder {

            @Test
            void postMilkOrder_returns201_whenValidRequest() throws Exception {
                // Arrange
                var request = Instancio.create(MilkOrderRequest.class);
                var response = Instancio.create(MilkOrderResponse.class);

                given(milkOrderService.create(request)).willReturn(response);

                // Act + Assert
                mockMvc.perform(post(ApiPaths.MILK_ORDERS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(header().string("Location", ApiPaths.MILK_ORDERS + "/" + response.id()))
                        .andExpect(jsonPath("$.id").value(response.id().toString()));
            }

            @Test
            void getMilkOrderById_returns200_whenExists() throws Exception {
                // Arrange
                var response = Instancio.create(MilkOrderResponse.class);

                given(milkOrderService.getById(response.id())).willReturn(response);

                // Act + Assert
                mockMvc.perform(get(ApiPaths.MILK_ORDERS + "/{id}", response.id()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(response.id().toString()));
            }

            @Test
            void listMilkOrders_returns200_withData() throws Exception {
                // Arrange
                var page = new PageImpl<>(Instancio.ofList(MilkOrderResponse.class).size(2).create());

                given(milkOrderService.search(any(), any())).willReturn(page);

                // Act + Assert
                mockMvc.perform(get(ApiPaths.MILK_ORDERS))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content.size()").value(2));
            }

            @Test
            void listMilkOrders_returns200_whenFilteredByCustomerRef() throws Exception {
                var page = new PageImpl<>(Instancio.ofList(MilkOrderResponse.class).size(2).create());

                // Arrange
                given(milkOrderService.search(eq("REF123"), any())).willReturn(page);

                // Act + Assert
                mockMvc.perform(get(ApiPaths.MILK_ORDERS).param("customerRef", "REF123"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content.size()").value(2));
            }
        }

        @Nested
        class OrderLine {

            @Test
            void postOrderLine_returns201_whenValidRequest() throws Exception {
                // Arrange
                var id = UUID.randomUUID();
                var request = Instancio.create(OrderLineCreateRequest.class);
                var response = Instancio.of(OrderLineResponse.class).set(field(OrderLineResponse::id), id).create();

                given(milkOrderService.addLine(id, request)).willReturn(response);

                // Act + Assert
                mockMvc.perform(post(ApiPaths.MILK_ORDERS + "/{id}" + ApiPaths.LINES, id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(header().string("Location", ApiPaths.MILK_ORDERS + "/" + id + ApiPaths.LINES + "/" + response.id()))
                        .andExpect(jsonPath("$.id").value(id.toString()));
            }

            @Test
            void putOrderLine_returns200_whenExists() throws Exception {
                // Arrange
                var orderId = UUID.randomUUID();
                var lineId = UUID.randomUUID();
                var request = Instancio.create(OrderLineUpdateRequest.class);
                var response = Instancio.of(OrderLineResponse.class).set(field(OrderLineResponse::id), lineId).create();

                given(milkOrderService.updateLine(orderId, lineId, request)).willReturn(response);

                // Act + Assert
                mockMvc.perform(put(ApiPaths.MILK_ORDERS + "/{id}" + ApiPaths.LINES + "/{id}", orderId, lineId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(lineId.toString()));
            }

            @Test
            void deleteOrderLine_returns204_whenExists() throws Exception {
                // Arrange
                var orderId = UUID.randomUUID();
                var lineId = UUID.randomUUID();

                // Act + Assert
                mockMvc.perform(delete(ApiPaths.MILK_ORDERS + "/{id}" + ApiPaths.LINES + "/{id}", orderId, lineId))
                        .andExpect(status().isNoContent());

                // Assert
                verify(milkOrderService).removeLine(orderId, lineId);
            }
        }
    }

    // ---------------
    //    NEGATIVE
    // ---------------

    @Nested
    class NegativeTests {

        @Nested
        class MilkOrder {
            @Test
            void postMilkOrder_returns400_whenInvalidData() throws Exception {
                // Arrange
                var invalidRequest = new MilkOrderRequest(null, null, null);

                // Act + Assert
                mockMvc.perform(post(ApiPaths.MILK_ORDERS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors.customerRef").exists())
                        .andExpect(jsonPath("$.errors.customerId").exists());

                // Assert
                verifyNoInteractions(milkOrderService);
            }

            @Test
            void postMilk_returns409_whenDuplicatedCustomerRef() throws Exception {
                // Arrange
                var request = Instancio.create(MilkOrderRequest.class);

                given(milkOrderService.create(request)).willThrow(new DataIntegrityViolationException("Conflict"));

                // Act + Assert
                mockMvc.perform(post(ApiPaths.MILK_ORDERS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isConflict());
            }

            @Test
            void getMilkOrder_returns404_whenNotFound() throws Exception {
                // Arrange
                var id = UUID.randomUUID();

                given(milkOrderService.getById(id)).willThrow(new NotFoundException("Not found"));

                // Act + Assert
                mockMvc.perform(get(ApiPaths.MILK_ORDERS + "/{id}", id))
                        .andExpect(status().isNotFound());
            }
        }

        @Nested
        class OrderLine {

            @Test
            void postOrderLine_returns400_whenInvalidData() throws Exception {
                // Arrange
                var id = UUID.randomUUID();
                var invalidRequest = new OrderLineCreateRequest(-1, id);

                // Act + Assert
                mockMvc.perform(post(ApiPaths.MILK_ORDERS + "/{id}" + ApiPaths.LINES, id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors.requestedQuantity").exists());

                // Assert
                verifyNoInteractions(milkOrderService);
            }

            @Test
            void putOrderLine_returns400_whenInvalidData() throws Exception {
                // Arrange
                var orderId = UUID.randomUUID();
                var lineId = UUID.randomUUID();
                var invalidRequest = new OrderLineUpdateRequest(-1);

                // Act + Assert
                mockMvc.perform(put(ApiPaths.MILK_ORDERS + "/{id}" + ApiPaths.LINES + "/{id}", orderId, lineId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors.requestedQuantity").exists());

                // Assert
                verifyNoInteractions(milkOrderService);
            }

            @Test
            void deleteOrderLiner_returns404_whenNotFound() throws Exception {
                // Arrange
                var orderId = UUID.randomUUID();
                var lineId = UUID.randomUUID();

                willThrow(new NotFoundException("Not found")).given(milkOrderService).removeLine(orderId, lineId);

                // Act + Assert
                mockMvc.perform(delete(ApiPaths.MILK_ORDERS + "/{id}" + ApiPaths.LINES + "/{id}", orderId, lineId)
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNotFound());

                // Assert
                verify(milkOrderService).removeLine(orderId, lineId);
            }
        }
    }
}