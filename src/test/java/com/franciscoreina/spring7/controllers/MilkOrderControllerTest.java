package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.domain.order.OrderLine;
import com.franciscoreina.spring7.dto.request.order.MilkOrderRequest;
import com.franciscoreina.spring7.dto.request.order.OrderLineCreateRequest;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import com.franciscoreina.spring7.services.MilkOrderService;
import com.franciscoreina.spring7.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    Customer savedCustomer;
    OrderLine savedOrderLine;
    MilkOrder savedMilkOrder;
    MilkOrderRequest milkOrderCreateRequest;
    MilkOrderResponse milkOrderResponse;
    OrderLineCreateRequest orderLineCreateRequest;

    @BeforeEach
    void setUp() {
        var savedCategory = TestDataFactory.getSavedCategory(TestDataFactory.getNewCategory());
        savedCustomer = TestDataFactory.getSavedCustomer(TestDataFactory.getNewCustomer());
        var customerResponse = TestDataFactory.getCustomerResponse(savedCustomer);
        var savedMilk = TestDataFactory.getSavedMilk(TestDataFactory.getNewMilk(savedCategory));
        var milkResponse = TestDataFactory.getMilkResponse(savedMilk);

        savedOrderLine = TestDataFactory.getSavedOrderLine(savedMilk);
        savedMilkOrder = TestDataFactory.getSavedMilkOrder(savedCustomer, Set.of(savedOrderLine)); // MilkOrder is assigned to OrderLine
        orderLineCreateRequest = TestDataFactory.getOrderLineCreateRequest(milkResponse.id());
        milkOrderCreateRequest = TestDataFactory.getMilkOrderCreateRequest(customerResponse.id(), orderLineCreateRequest);
        milkOrderResponse = TestDataFactory.getMilkOrderResponse(savedMilkOrder);
    }

    @Test
    void postMilkOrder_returns201_andLocationHeader_whenRequestValid() throws Exception {
        // Arrange
        given(milkOrderService.create(milkOrderCreateRequest)).willReturn(milkOrderResponse);

        // Act
        mockMvc.perform(post(ApiPaths.MILK_ORDERS)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(milkOrderCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", ApiPaths.MILK_ORDERS + "/" + milkOrderResponse.id()));

        // Assert
        verify(milkOrderService).create(milkOrderCreateRequest);
    }

    @Test
    void getMilkOrderById_returns200_andBody_whenExists() throws Exception {
        // Arrange
        var savedMilkOrderId = savedMilkOrder.getId();
        given(milkOrderService.getById(savedMilkOrderId)).willReturn(milkOrderResponse);

        // Act
        mockMvc.perform(get(ApiPaths.MILK_ORDERS + "/" + savedMilkOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(milkOrderResponse.id().toString()))
                .andExpect(jsonPath("$.customerRef").value(milkOrderResponse.customerRef()));

        // Assert
        verify(milkOrderService).getById(savedMilkOrderId);
    }

    @Test
    void listMilkOrders_returns200_andArray_whenExists() throws Exception {
        // Arrange
        var savedMilkOrder2 = TestDataFactory.getSavedMilkOrder(savedCustomer, Set.of(savedOrderLine));
        var response2 = TestDataFactory.getMilkOrderResponse(savedMilkOrder2);
        var responseList = new PageImpl<>(List.of(milkOrderResponse, response2));

        given(milkOrderService.list(isNull(), any(Pageable.class))).willReturn(responseList);

        // Act
        mockMvc.perform(get(ApiPaths.MILK_ORDERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(milkOrderResponse.id().toString()))
                .andExpect(jsonPath("$.content[0].customerRef").value(milkOrderResponse.customerRef()))
                .andExpect(jsonPath("$.content[1].id").value(response2.id().toString()))
                .andExpect(jsonPath("$.content[1].customerRef").value(response2.customerRef()));

        // Assert
        verify(milkOrderService).list(isNull(), any(Pageable.class));
    }
}