package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.domain.order.MilkOrderStatus;
import com.franciscoreina.spring7.domain.order.OrderLineStatus;
import com.franciscoreina.spring7.dto.request.order.MilkOrderRequest;
import com.franciscoreina.spring7.dto.request.order.OrderLineCreateRequest;
import com.franciscoreina.spring7.dto.request.order.OrderLineUpdateRequest;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import com.franciscoreina.spring7.dto.response.order.OrderLineResponse;
import com.franciscoreina.spring7.services.MilkOrderService;
import org.instancio.Instancio;
import org.instancio.Model;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.mockito.BDDMockito.given;
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
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private MilkOrderService milkOrderService;

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID MILK_ID = UUID.randomUUID();
    private static final UUID ORDER_LINE_ID = UUID.randomUUID();

    private static final Model<OrderLineCreateRequest> ORDER_LINE_CREATE_REQUEST_MODEL =
            Instancio.of(OrderLineCreateRequest.class)
                    .set(field(OrderLineCreateRequest::requestedQuantity), 3)
                    .set(field(OrderLineCreateRequest::milkId), MILK_ID)
                    .toModel();

    private static final Model<OrderLineUpdateRequest> ORDER_LINE_UPDATE_REQUEST_MODEL =
            Instancio.of(OrderLineUpdateRequest.class)
                    .set(field(OrderLineUpdateRequest::requestedQuantity), 5)
                    .toModel();

    private static final Model<MilkOrderRequest> MILK_ORDER_REQUEST_MODEL =
            Instancio.of(MilkOrderRequest.class)
                    .set(field(MilkOrderRequest::customerRef), "ORDER-123")
                    .set(field(MilkOrderRequest::customerId), CUSTOMER_ID)
                    .set(field(MilkOrderRequest::orderLines), Set.of(
                            new OrderLineCreateRequest(2, MILK_ID)
                    ))
                    .toModel();

    @Nested
    class CreateTests {

        @Test
        void create_shouldReturnCreated_whenRequestIsValid() throws Exception {
            // Arrange
            var request = Instancio.create(MILK_ORDER_REQUEST_MODEL);
            var response = orderResponse(
                    request.customerRef(),
                    CUSTOMER_ID,
                    Set.of(orderLineResponse(ORDER_LINE_ID, 2, 0, MILK_ID))
            );

            given(milkOrderService.create(request)).willReturn(response);

            // Act + Assert
            mockMvc.perform(post(ApiPaths.MILK_ORDERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION, ApiPaths.MILK_ORDERS + "/" + ORDER_ID))
                    .andExpect(jsonPath("$.id").value(response.id().toString()))
                    .andExpect(jsonPath("$.customerRef").value(response.customerRef()))
                    .andExpect(jsonPath("$.customerId").value(response.customerId().toString()))
                    .andExpect(jsonPath("$.milkOrderStatus").value(response.milkOrderStatus().name()))
                    .andExpect(jsonPath("$.paymentAmount").value(response.paymentAmount().doubleValue()))
                    .andExpect(jsonPath("$.orderLines[0].id").value(ORDER_LINE_ID.toString()));

            verify(milkOrderService).create(request);
        }

        @Test
        void create_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
            // Arrange
            var request = new MilkOrderRequest(
                    "",
                    null,
                    Set.of()
            );

            // Act + Assert
            mockMvc.perform(post(ApiPaths.MILK_ORDERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(milkOrderService);
        }

        @Test
        void create_shouldReturnBadRequest_whenCustomerIdIsNull() throws Exception {
            // Arrange
            var request = new MilkOrderRequest(
                    "ORDER-123",
                    null,
                    Set.of(new OrderLineCreateRequest(2, MILK_ID))
            );

            // Act + Assert
            mockMvc.perform(post(ApiPaths.MILK_ORDERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.customerId").exists());

            verifyNoInteractions(milkOrderService);
        }
    }

    @Nested
    class GetByIdTests {

        @Test
        void getById_shouldReturnOk_whenOrderExists() throws Exception {
            // Arrange
            var response = orderResponse(
                    "ORDER-123",
                    CUSTOMER_ID,
                    Set.of(orderLineResponse(ORDER_LINE_ID, 2, 0, MILK_ID))
            );

            given(milkOrderService.getById(ORDER_ID)).willReturn(response);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.MILK_ORDERS + "/" + ORDER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(response.id().toString()))
                    .andExpect(jsonPath("$.customerRef").value(response.customerRef()))
                    .andExpect(jsonPath("$.customerId").value(response.customerId().toString()))
                    .andExpect(jsonPath("$.orderLines[0].id").value(ORDER_LINE_ID.toString()));

            verify(milkOrderService).getById(ORDER_ID);
        }
    }

    @Nested
    class SearchTests {

        @Test
        void search_shouldReturnPage_whenNoFilterIsProvided() throws Exception {
            // Arrange
            var response = orderResponse(
                    "ORDER-123",
                    CUSTOMER_ID,
                    Set.of(orderLineResponse(ORDER_LINE_ID, 2, 0, MILK_ID))
            );
            var page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);

            given(milkOrderService.search(null, PageRequest.of(0, 20))).willReturn(page);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.MILK_ORDERS)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(response.id().toString()))
                    .andExpect(jsonPath("$.content[0].customerRef").value(response.customerRef()));

            verify(milkOrderService).search(null, PageRequest.of(0, 20));
        }

        @Test
        void search_shouldReturnPage_whenCustomerRefFilterIsProvided() throws Exception {
            // Arrange
            var response = orderResponse(
                    "ORDER-123",
                    CUSTOMER_ID,
                    Set.of(orderLineResponse(ORDER_LINE_ID, 2, 0, MILK_ID))
            );
            var page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);

            given(milkOrderService.search("ORDER", PageRequest.of(0, 20))).willReturn(page);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.MILK_ORDERS)
                            .param("customerRef", "ORDER")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(response.id().toString()));

            verify(milkOrderService).search("ORDER", PageRequest.of(0, 20));
        }
    }

    @Nested
    class AddLineTests {

        @Test
        void addLine_shouldReturnCreated_whenRequestIsValid() throws Exception {
            // Arrange
            var request = Instancio.create(ORDER_LINE_CREATE_REQUEST_MODEL);
            var response = orderLineResponse(ORDER_LINE_ID, request.requestedQuantity(), 0, request.milkId());

            given(milkOrderService.addLine(ORDER_ID, request)).willReturn(response);

            // Act + Assert
            mockMvc.perform(post(ApiPaths.MILK_ORDERS + "/" + ORDER_ID + "/lines")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(response.id().toString()))
                    .andExpect(jsonPath("$.requestedQuantity").value(response.requestedQuantity()))
                    .andExpect(jsonPath("$.milkId").value(response.milkId().toString()));

            verify(milkOrderService).addLine(ORDER_ID, request);
        }

        @Test
        void addLine_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
            // Arrange
            var request = new OrderLineCreateRequest(0, null);

            // Act + Assert
            mockMvc.perform(post(ApiPaths.MILK_ORDERS + "/" + ORDER_ID + "/lines")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(milkOrderService);
        }
    }

    @Nested
    class UpdateLineTests {

        @Test
        void updateLine_shouldReturnOk_whenRequestIsValid() throws Exception {
            // Arrange
            var request = Instancio.create(ORDER_LINE_UPDATE_REQUEST_MODEL);
            var response = orderLineResponse(ORDER_LINE_ID, request.requestedQuantity(), 0, MILK_ID);

            given(milkOrderService.updateLine(ORDER_ID, ORDER_LINE_ID, request)).willReturn(response);

            // Act + Assert
            mockMvc.perform(put(ApiPaths.MILK_ORDERS + "/" + ORDER_ID + "/lines/" + ORDER_LINE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(response.id().toString()))
                    .andExpect(jsonPath("$.requestedQuantity").value(response.requestedQuantity()));

            verify(milkOrderService).updateLine(ORDER_ID, ORDER_LINE_ID, request);
        }

        @Test
        void updateLine_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
            // Arrange
            var request = new OrderLineUpdateRequest(0);

            // Act + Assert
            mockMvc.perform(put(ApiPaths.MILK_ORDERS + "/" + ORDER_ID + "/lines/" + ORDER_LINE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(milkOrderService);
        }
    }

    @Nested
    class RemoveLineTests {

        @Test
        void removeLine_shouldReturnNoContent_whenLineExists() throws Exception {
            // Act + Assert
            mockMvc.perform(delete(ApiPaths.MILK_ORDERS + "/" + ORDER_ID + "/lines/" + ORDER_LINE_ID))
                    .andExpect(status().isNoContent());

            verify(milkOrderService).removeLine(ORDER_ID, ORDER_LINE_ID);
        }
    }

    private MilkOrderResponse orderResponse(String customerRef,
                                            UUID customerId,
                                            Set<OrderLineResponse> orderLines) {
        return new MilkOrderResponse(
                ORDER_ID,
                Instant.parse("2026-04-03T10:00:00Z"),
                Instant.parse("2026-04-03T10:00:00Z"),
                customerRef,
                new BigDecimal("5.00"),
                MilkOrderStatus.NEW,
                customerId,
                orderLines,
                null
        );
    }

    private OrderLineResponse orderLineResponse(UUID orderLineId,
                                                Integer requestedQuantity,
                                                Integer assignedQuantity,
                                                UUID milkId) {
        return new OrderLineResponse(
                orderLineId,
                Instant.parse("2026-04-03T10:00:00Z"),
                Instant.parse("2026-04-03T10:00:00Z"),
                requestedQuantity,
                assignedQuantity,
                OrderLineStatus.NEW,
                new BigDecimal("2.50"),
                ORDER_ID,
                milkId
        );
    }
}
