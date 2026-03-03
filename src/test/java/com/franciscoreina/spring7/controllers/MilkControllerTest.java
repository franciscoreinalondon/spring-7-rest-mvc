package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.domain.Milk;
import com.franciscoreina.spring7.dtos.milk.MilkCreateRequest;
import com.franciscoreina.spring7.dtos.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dtos.milk.MilkResponse;
import com.franciscoreina.spring7.dtos.milk.MilkUpdateRequest;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.services.MilkService;
import com.franciscoreina.spring7.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MilkController.class)
public class MilkControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    MilkService milkService;

    Milk milk;
    Milk savedMilk;
    MilkCreateRequest milkCreateRequest;
    MilkUpdateRequest milkUpdateRequest;
    MilkPatchRequest milkPatchRequest;
    MilkResponse milkResponse;

    @BeforeEach
    void setUp() {
        milk = TestDataFactory.newMilk();
        savedMilk = TestDataFactory.newSavedMilk(milk);
        milkCreateRequest = TestDataFactory.newMilkCreateRequest(milk);
        milkUpdateRequest = TestDataFactory.newMilkUpdateRequest(savedMilk);
        milkPatchRequest = TestDataFactory.newMilkPatchRequestWithName();
        milkResponse = TestDataFactory.newMilkResponse(savedMilk);
    }

    @Test
    void postMilk_returns201_andLocationHeader_whenRequestValid() throws Exception {
        // Arrange
        given(milkService.create(milkCreateRequest)).willReturn(milkResponse);

        // Act
        mockMvc.perform(post(ApiPaths.MILKS)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(milkCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", ApiPaths.MILKS + "/" + milkResponse.id()));

        // Assert
        verify(milkService).create(milkCreateRequest);
    }


    @Test
    void postMilk_returns400_whenNameNull() throws Exception {
        // Arrange
        milk.setName(null);
        MilkCreateRequest wrongCreateRequest = TestDataFactory.newMilkCreateRequest(milk);

        // Act
        mockMvc.perform(post(ApiPaths.MILKS)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(wrongCreateRequest)))
                .andExpect(status().isBadRequest());

        // Assert
        verifyNoInteractions(milkService);
    }

    @Test
    void postMilk_returns409_whenUpcDuplicated() throws Exception {
        // Arrange
        willThrow(new DataIntegrityViolationException("Upc Duplicated")).given(milkService).create(milkCreateRequest);

        // Act
        mockMvc.perform(post(ApiPaths.MILKS)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(milkCreateRequest)))
                .andExpect(status().isConflict());

        // Assert
        verify(milkService).create(milkCreateRequest);
    }

    @Test
    void getMilkById_returns200_andBody_whenExists() throws Exception {
        // Arrange
        UUID milkId = savedMilk.getId();
        given(milkService.getById(milkId)).willReturn(milkResponse);

        // Act
        mockMvc.perform(get(ApiPaths.MILKS + "/" + milkId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(milkResponse.id().toString()))
                .andExpect(jsonPath("$.name").value(milkResponse.name()))
                .andExpect(jsonPath("$.upc").value(milkResponse.upc()));

        // Assert
        verify(milkService).getById(milkId);
    }

    @Test
    void getMilkById_returns404_whenMissing() throws Exception {
        // Arrange
        UUID milkId = savedMilk.getId();
        given(milkService.getById(milkId)).willThrow(NotFoundException.class);

        // Act
        mockMvc.perform(get(ApiPaths.MILKS + "/" + milkId))
                .andExpect(status().isNotFound());

        // Assert
        verify(milkService).getById(milkId);
    }

    @Test
    void listMilks_returns200_andArray_whenExists() throws Exception {
        // Arrange
        Milk savedMilk2 = TestDataFactory.newSavedMilk(TestDataFactory.newMilk());
        MilkResponse response2 = TestDataFactory.newMilkResponse(savedMilk2);
        List<MilkResponse> responseList = List.of(milkResponse, response2);

        given(milkService.list()).willReturn(responseList);

        // Act
        mockMvc.perform(get(ApiPaths.MILKS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(milkResponse.id().toString()))
                .andExpect(jsonPath("$[0].upc").value(milkResponse.upc()))
                .andExpect(jsonPath("$[1].id").value(response2.id().toString()))
                .andExpect(jsonPath("$[1].upc").value(response2.upc()));

        // Assert
        verify(milkService).list();
    }

    @Test
    void listMilks_returns200_andEmptyArray_whenNotExists() throws Exception {
        // Arrange
        given(milkService.list()).willReturn(Collections.emptyList());

        // Act
        mockMvc.perform(get(ApiPaths.MILKS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        // Assert
        verify(milkService).list();
    }

    @Test
    void putMilk_returns204_whenRequestValid_andExists() throws Exception {
        // Arrange
        UUID milkId = savedMilk.getId();

        // Act
        mockMvc.perform(put(ApiPaths.MILKS + "/" + milkId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(milkUpdateRequest)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // Assert
        verify(milkService).update(milkId, milkUpdateRequest);
    }

    @Test
    void putMilk_returns400_whenNameNull() throws Exception {
        // Arrange
        UUID milkId = savedMilk.getId();
        milk.setName(null);
        MilkUpdateRequest wrongUpdateRequest = TestDataFactory.newMilkUpdateRequest(milk);

        // Act
        mockMvc.perform(put(ApiPaths.MILKS + "/" + milkId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongUpdateRequest)))
                .andExpect(status().isBadRequest());

        // Assert
        verifyNoInteractions(milkService);
    }

    @Test
    void putMilk_returns404_whenMissing() throws Exception {
        // Arrange
        UUID milkId = savedMilk.getId();
        willThrow(NotFoundException.class).given(milkService).update(milkId, milkUpdateRequest);

        // Act
        mockMvc.perform(put(ApiPaths.MILKS + "/" + milkId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(milkUpdateRequest)))
                .andExpect(status().isNotFound());

        // Assert
        verify(milkService).update(milkId, milkUpdateRequest);
    }

    @Test
    void putMilk_returns409_whenUpcDuplicated() throws Exception {
        // Arrange
        UUID milkId = savedMilk.getId();
        willThrow(new DataIntegrityViolationException("Upc Duplicated")).given(milkService).update(milkId, milkUpdateRequest);

        // Act
        mockMvc.perform(put(ApiPaths.MILKS + "/" + milkId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(milkUpdateRequest)))
                .andExpect(status().isConflict());

        // Assert
        verify(milkService).update(milkId, milkUpdateRequest);
    }

    @Test
    void patchMilk_returns204_whenRequestValid_andExists() throws Exception {
        // Arrange
        UUID milkId = savedMilk.getId();

        // Act
        mockMvc.perform(patch(ApiPaths.MILKS + "/" + milkId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(milkPatchRequest)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));


        // Assert
        verify(milkService).patch(milkId, milkPatchRequest);
    }

    @Test
    void patchMilk_returns400_whenUpcInvalid() throws Exception {
        // Arrange
        UUID milkId = savedMilk.getId();
        String invalidUpc = TestDataFactory.randomText(55);
        MilkPatchRequest wrongPatchRequest = new MilkPatchRequest(null, null, invalidUpc, null, null);

        // Act
        mockMvc.perform(patch(ApiPaths.MILKS + "/" + milkId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPatchRequest)))
                .andExpect(status().isBadRequest());

        // Assert
        verifyNoInteractions(milkService);
    }

    @Test
    void patchMilk_returns404_whenMissing() throws Exception {
        // Arrange
        UUID milkId = savedMilk.getId();
        willThrow(NotFoundException.class).given(milkService).patch(milkId, milkPatchRequest);

        // Act
        mockMvc.perform(patch(ApiPaths.MILKS + "/" + milkId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(milkPatchRequest)))
                .andExpect(status().isNotFound());

        // Assert
        verify(milkService).patch(milkId, milkPatchRequest);
    }

    @Test
    void patchMilk_returns409_whenUpcDuplicated() throws Exception {
        // Arrange
        UUID milkId = savedMilk.getId();
        willThrow(new DataIntegrityViolationException("Upc Duplicated")).given(milkService).patch(milkId, milkPatchRequest);

        // Act
        mockMvc.perform(patch(ApiPaths.MILKS + "/" + milkId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(milkPatchRequest)))
                .andExpect(status().isConflict());

        // Assert
        verify(milkService).patch(milkId, milkPatchRequest);
    }

    @Test
    void deleteMilk_returns204_whenExists() throws Exception {
        // Arrange
        UUID milkId = savedMilk.getId();

        // Act
        mockMvc.perform(delete(ApiPaths.MILKS + "/" + milkId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));


        // Assert
        verify(milkService).delete(milkId);
    }

    @Test
    void deleteMilk_returns404_whenMissing() throws Exception {
        // Arrange
        UUID milkId = savedMilk.getId();
        willThrow(NotFoundException.class).given(milkService).delete(milkId);

        // Act
        mockMvc.perform(delete(ApiPaths.MILKS + "/" + milkId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Assert
        verify(milkService).delete(milkId);
    }
}
