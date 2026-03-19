package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.request.milk.MilkCreateRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import com.franciscoreina.spring7.dto.request.milk.MilkUpdateRequest;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.services.MilkService;
import com.franciscoreina.spring7.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
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

@ImportAutoConfiguration(exclude = OAuth2ResourceServerAutoConfiguration.class)
@WebMvcTest(MilkController.class)
public class MilkControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    MilkService milkService;

    @MockitoBean
    JwtDecoder jwtDecoder;

    Category savedCategory;
    Milk milk;
    Milk savedMilk;
    MilkCreateRequest milkCreateRequest;
    MilkUpdateRequest milkUpdateRequest;
    MilkPatchRequest milkPatchRequest;
    MilkResponse milkResponse;

    @BeforeEach
    void setUp() {
        savedCategory = TestDataFactory.newSavedCategory();
        milk = TestDataFactory.newMilk(savedCategory);
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
        Milk savedMilk2 = TestDataFactory.newSavedMilk(TestDataFactory.newMilk(savedCategory));
        MilkResponse response2 = TestDataFactory.newMilkResponse(savedMilk2);
        Page<MilkResponse> responseList = new PageImpl<>(List.of(milkResponse, response2));

        given(milkService.list(isNull(), isNull(), any(Pageable.class))).willReturn(responseList);

        // Act
        mockMvc.perform(get(ApiPaths.MILKS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(milkResponse.id().toString()))
                .andExpect(jsonPath("$.content[0].upc").value(milkResponse.upc()))
                .andExpect(jsonPath("$.content[1].id").value(response2.id().toString()))
                .andExpect(jsonPath("$.content[1].upc").value(response2.upc()));

        // Assert
        verify(milkService).list(isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void listMilksByName_returns200_andArray_whenExists() throws Exception {
        // Arrange
        Milk milk1 = TestDataFactory.newMilk(savedCategory);
        milk1.setName("Ultra-Fresh Skimmed");
        milk1.setMilkType(MilkType.SKIMMED);
        MilkResponse response1 = TestDataFactory.newMilkResponse(TestDataFactory.newSavedMilk(milk1));

        Milk milk2 = TestDataFactory.newMilk(savedCategory);
        milk2.setName("Select Semi Skimmed");
        MilkResponse response2 = TestDataFactory.newMilkResponse(TestDataFactory.newSavedMilk(milk2));

        Pageable pageable = PageRequest.of(0, 20);

        Page<MilkResponse> responseList = new PageImpl<>(List.of(response1, response2));

        given(milkService.list("skimmed", null, pageable)).willReturn(responseList);

        // Act
        mockMvc.perform(get(ApiPaths.MILKS)
                        .param("name", "skimmed")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(response1.id().toString()))
                .andExpect(jsonPath("$.content[0].name").value(response1.name()));

        // Assert
        verify(milkService).list("skimmed", null, pageable);
    }

    @Test
    void listMilksByType_returns200_andArray_whenExists() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);

        Page<MilkResponse> responseList = new PageImpl<>(List.of(milkResponse));

        given(milkService.list(null, MilkType.SEMI_SKIMMED, pageable)).willReturn(responseList);

        // Act
        mockMvc.perform(get(ApiPaths.MILKS)
                        .param("milkType", "SEMI_SKIMMED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(milkResponse.id().toString()))
                .andExpect(jsonPath("$.content[0].milkType").value(String.valueOf(milkResponse.milkType())));

        // Assert
        verify(milkService).list(null, MilkType.SEMI_SKIMMED, pageable);
    }

    @Test
    void listMilksByNameAndType_returns200_andArray_whenExists() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);

        Page<MilkResponse> responseList = new PageImpl<>(List.of(milkResponse));

        given(milkService.list("Milk name", MilkType.SEMI_SKIMMED, pageable)).willReturn(responseList);

        // Act
        mockMvc.perform(get(ApiPaths.MILKS)
                        .param("name", "Milk name")
                        .param("milkType", "SEMI_SKIMMED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(milkResponse.id().toString()))
                .andExpect(jsonPath("$.content[0].name").value(milkResponse.name()))
                .andExpect(jsonPath("$.content[0].milkType").value(String.valueOf(milkResponse.milkType())));

        // Assert
        verify(milkService).list("Milk name", MilkType.SEMI_SKIMMED, pageable);
    }

    @Test
    void listMilks_returns200_andEmptyArray_whenNotExists() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);

        given(milkService.list(null, null, pageable)).willReturn(Page.empty());

        // Act
        mockMvc.perform(get(ApiPaths.MILKS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(0));

        // Assert
        verify(milkService).list(null, null, pageable);
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
        MilkPatchRequest wrongPatchRequest = TestDataFactory.newMilkPatchRequestInvalidUpc();

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
