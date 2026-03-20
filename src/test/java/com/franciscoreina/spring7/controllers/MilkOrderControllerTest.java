package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.dto.request.order.MilkOrderCreateRequest;
import com.franciscoreina.spring7.dto.request.order.OrderLineCreateRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import com.franciscoreina.spring7.services.MilkOrderService;
import com.franciscoreina.spring7.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

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
@WebMvcTest
class MilkOrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MilkOrderService milkOrderService;

    Category category;
    Customer customer;
    CustomerResponse customerResponse;
    Milk milk;
    MilkResponse milkResponse;
    OrderLineCreateRequest orderLineCreateRequest;
    MilkOrder savedMilkOrder;
    MilkOrderCreateRequest milkOrderCreateRequest;
    MilkOrderResponse milkOrderResponse;

    @BeforeEach
    void setUp() {
        category = TestDataFactory.getNewCategory();
        customer = TestDataFactory.getNewCustomer();
        customerResponse = TestDataFactory.getCustomerResponse(customer);
        milk = TestDataFactory.getNewMilk(category);
        milkResponse = TestDataFactory.getMilkResponse(milk);
        orderLineCreateRequest = TestDataFactory.newOrderLineCreateRequest(milkResponse);
//        savedMilkOrder = TestDataFactory.
        milkOrderCreateRequest = TestDataFactory.newMilkOrderCreateRequest(customerResponse, orderLineCreateRequest);
        milkOrderResponse = TestDataFactory.newMilkOrderResponse(savedMilkOrder);
    }

//    @Test
    void postMilkOrder_returns201_andLocationHeader_whenRequestValid() throws Exception {
        // Arrange
        given(milkOrderService.create(milkOrderCreateRequest)).willReturn(milkOrderResponse);

        // Act
        mockMvc.perform(post(ApiPaths.MILK_ORDERS)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(milkOrderCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", ApiPaths.MILKS + "/" + milkOrderResponse.id()));

        // Assert
        verify(milkOrderService).create(milkOrderCreateRequest);
    }

//    @Test
    void getMilkOrderById_returns200_andBody_whenExists() throws Exception {
        // Arrange
        UUID milkOrderId = savedMilkOrder.getId();
        given(milkOrderService.getById(milkOrderId)).willReturn(milkOrderResponse);

        // Act
        mockMvc.perform(get(ApiPaths.MILK_ORDERS + "/" + milkOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(milkOrderResponse.id().toString()));
//                .andExpect(jsonPath("$.name").value(milkResponse.name()))
//                .andExpect(jsonPath("$.upc").value(milkResponse.upc()));

        // Assert
        verify(milkOrderService).getById(milkOrderId);
    }

//    @Test
    void listMilkOrders_returns200_andArray_whenExists() throws Exception {
        // Arrange
//        MilkOrderResponse savedMilkOrder2 = TestDataFactory.newSavedMilkOrder(TestDataFactory.newMilkOrder(savedCategory));

//        MilkOrderResponse response2 = TestDataFactory.newMilkOrderResponse(savedMilkOrder2);
//        Page<MilkOrderResponse> responseList = new PageImpl<>(List.of(milkOrderResponse, response2));

//        given(milkOrderService.list(isNull(), isNull(), any(Pageable.class))).willReturn(responseList);

        // Act
//        mockMvc.perform(get(ApiPaths.MILK_ORDERS))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content.size()").value(2))
//                .andExpect(jsonPath("$.content[0].id").value(milkOrderResponse.id().toString()))
////                .andExpect(jsonPath("$.content[0].upc").value(milkResponse.upc()))
//                .andExpect(jsonPath("$.content[1].id").value(response2.id().toString()));
////                .andExpect(jsonPath("$.content[1].upc").value(response2.upc()));

        // Assert
        verify(milkOrderService).list(isNull(), isNull(), any(Pageable.class));
    }
}