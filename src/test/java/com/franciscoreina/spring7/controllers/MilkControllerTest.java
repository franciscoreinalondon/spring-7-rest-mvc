package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.request.milk.MilkCreateRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkUpdateRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
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
    Milk newMilk;
    Milk savedMilk;
    MilkCreateRequest createRequest;
    MilkUpdateRequest updateRequest;
    MilkPatchRequest patchRequest;
    MilkResponse response;

    @BeforeEach
    void setUp() {
        savedCategory = TestDataFactory.getSavedCategory(TestDataFactory.getNewCategory());
        newMilk = TestDataFactory.getNewMilk(savedCategory);
        savedMilk = TestDataFactory.getSavedMilk(newMilk);
        createRequest = TestDataFactory.getMilkCreateRequest(newMilk);
        updateRequest = TestDataFactory.getMilkUpdateRequest(savedMilk);
        patchRequest = TestDataFactory.getMilkPatchRequestWithName();
        response = TestDataFactory.getMilkResponse(savedMilk);
    }

    @Test
    void postMilk_returns201_andLocationHeader_whenRequestValid() throws Exception {
        // Arrange
        given(milkService.create(createRequest)).willReturn(response);

        // Act
        mockMvc.perform(post(ApiPaths.MILKS)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", ApiPaths.MILKS + "/" + response.id()));

        // Assert
        verify(milkService).create(createRequest);
    }


    @Test
    void postMilk_returns400_whenNameNull() throws Exception {
        // Arrange
        newMilk.setName(null);
        MilkCreateRequest wrongCreateRequest = TestDataFactory.getMilkCreateRequest(newMilk);

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
        willThrow(new DataIntegrityViolationException("Upc Duplicated")).given(milkService).create(createRequest);

        // Act
        mockMvc.perform(post(ApiPaths.MILKS)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(createRequest)))
                .andExpect(status().isConflict());

        // Assert
        verify(milkService).create(createRequest);
    }

    @Test
    void getMilkById_returns200_andBody_whenExists() throws Exception {
        // Arrange
        var savedMilkId = savedMilk.getId();
        given(milkService.getById(savedMilkId)).willReturn(response);

        // Act
        mockMvc.perform(get(ApiPaths.MILKS + "/" + savedMilkId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id().toString()))
                .andExpect(jsonPath("$.name").value(response.name()))
                .andExpect(jsonPath("$.upc").value(response.upc()));

        // Assert
        verify(milkService).getById(savedMilkId);
    }

    @Test
    void getMilkById_returns404_whenMissing() throws Exception {
        // Arrange
        var savedMilkId = savedMilk.getId();
        given(milkService.getById(savedMilkId)).willThrow(NotFoundException.class);

        // Act
        mockMvc.perform(get(ApiPaths.MILKS + "/" + savedMilkId))
                .andExpect(status().isNotFound());

        // Assert
        verify(milkService).getById(savedMilkId);
    }

    @Test
    void listMilks_returns200_andArray_whenExists() throws Exception {
        // Arrange
        var savedMilk2 = TestDataFactory.getSavedMilk(TestDataFactory.getNewMilk(savedCategory));
        var response2 = TestDataFactory.getMilkResponse(savedMilk2);
        var responseList = new PageImpl<>(List.of(response, response2));

        given(milkService.list(isNull(), isNull(), any(Pageable.class))).willReturn(responseList);

        // Act
        mockMvc.perform(get(ApiPaths.MILKS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(response.id().toString()))
                .andExpect(jsonPath("$.content[0].upc").value(response.upc()))
                .andExpect(jsonPath("$.content[1].id").value(response2.id().toString()))
                .andExpect(jsonPath("$.content[1].upc").value(response2.upc()));

        // Assert
        verify(milkService).list(isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void listMilksByName_returns200_andArray_whenExists() throws Exception {
        // Arrange
        var newMilk1 = TestDataFactory.getNewMilk(savedCategory);
        newMilk1.setName("Ultra-Fresh Skimmed");
        newMilk1.setMilkType(MilkType.SKIMMED);
        var response1 = TestDataFactory.getMilkResponse(TestDataFactory.getSavedMilk(newMilk1));

        var newMilk2 = TestDataFactory.getNewMilk(savedCategory);
        newMilk2.setName("Select Semi Skimmed");
        var response2 = TestDataFactory.getMilkResponse(TestDataFactory.getSavedMilk(newMilk2));

        var pageable = PageRequest.of(0, 20);
        var responseList = new PageImpl<>(List.of(response1, response2));

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
        var pageable = PageRequest.of(0, 20);
        var responseList = new PageImpl<>(List.of(response));

        given(milkService.list(null, MilkType.SEMI_SKIMMED, pageable)).willReturn(responseList);

        // Act
        mockMvc.perform(get(ApiPaths.MILKS)
                        .param("milkType", "SEMI_SKIMMED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(response.id().toString()))
                .andExpect(jsonPath("$.content[0].milkType").value(String.valueOf(response.milkType())));

        // Assert
        verify(milkService).list(null, MilkType.SEMI_SKIMMED, pageable);
    }

    @Test
    void listMilksByNameAndType_returns200_andArray_whenExists() throws Exception {
        // Arrange
        var pageable = PageRequest.of(0, 20);
        var responseList = new PageImpl<>(List.of(response));

        given(milkService.list("Milk name", MilkType.SEMI_SKIMMED, pageable)).willReturn(responseList);

        // Act
        mockMvc.perform(get(ApiPaths.MILKS)
                        .param("name", "Milk name")
                        .param("milkType", "SEMI_SKIMMED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(response.id().toString()))
                .andExpect(jsonPath("$.content[0].name").value(response.name()))
                .andExpect(jsonPath("$.content[0].milkType").value(String.valueOf(response.milkType())));

        // Assert
        verify(milkService).list("Milk name", MilkType.SEMI_SKIMMED, pageable);
    }

    @Test
    void listMilks_returns200_andEmptyArray_whenNotExists() throws Exception {
        // Arrange
        var pageable = PageRequest.of(0, 20);

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
        var savedMilkId = savedMilk.getId();

        // Act
        mockMvc.perform(put(ApiPaths.MILKS + "/" + savedMilkId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // Assert
        verify(milkService).update(savedMilkId, updateRequest);
    }

    @Test
    void putMilk_returns400_whenNameNull() throws Exception {
        // Arrange
        savedMilk.setName(null);
        var wrongUpdateRequest = TestDataFactory.getMilkUpdateRequest(savedMilk);

        // Act
        mockMvc.perform(put(ApiPaths.MILKS + "/" + savedMilk.getId())
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
        var savedMilkId = savedMilk.getId();
        willThrow(NotFoundException.class).given(milkService).update(savedMilkId, updateRequest);

        // Act
        mockMvc.perform(put(ApiPaths.MILKS + "/" + savedMilkId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        // Assert
        verify(milkService).update(savedMilkId, updateRequest);
    }

    @Test
    void putMilk_returns409_whenUpcDuplicated() throws Exception {
        // Arrange
        var savedMilkId = savedMilk.getId();
        willThrow(new DataIntegrityViolationException("Upc Duplicated")).given(milkService).update(savedMilkId, updateRequest);

        // Act
        mockMvc.perform(put(ApiPaths.MILKS + "/" + savedMilkId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict());

        // Assert
        verify(milkService).update(savedMilkId, updateRequest);
    }

    @Test
    void patchMilk_returns204_whenRequestValid_andExists() throws Exception {
        // Arrange
        var savedMilkId = savedMilk.getId();

        // Act
        mockMvc.perform(patch(ApiPaths.MILKS + "/" + savedMilkId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));


        // Assert
        verify(milkService).patch(savedMilkId, patchRequest);
    }

    @Test
    void patchMilk_returns400_whenUpcInvalid() throws Exception {
        // Arrange
        var savedMilkId = savedMilk.getId();
        var wrongPatchRequest = TestDataFactory.getMilkPatchRequestInvalidUpc();

        // Act
        mockMvc.perform(patch(ApiPaths.MILKS + "/" + savedMilkId)
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
        var savedMilkId = savedMilk.getId();
        willThrow(NotFoundException.class).given(milkService).patch(savedMilkId, patchRequest);

        // Act
        mockMvc.perform(patch(ApiPaths.MILKS + "/" + savedMilkId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isNotFound());

        // Assert
        verify(milkService).patch(savedMilkId, patchRequest);
    }

    @Test
    void patchMilk_returns409_whenUpcDuplicated() throws Exception {
        // Arrange
        var savedMilkId = savedMilk.getId();
        willThrow(new DataIntegrityViolationException("Upc Duplicated")).given(milkService).patch(savedMilkId, patchRequest);

        // Act
        mockMvc.perform(patch(ApiPaths.MILKS + "/" + savedMilkId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isConflict());

        // Assert
        verify(milkService).patch(savedMilkId, patchRequest);
    }

    @Test
    void deleteMilk_returns204_whenExists() throws Exception {
        // Arrange
        var savedMilkId = savedMilk.getId();

        // Act
        mockMvc.perform(delete(ApiPaths.MILKS + "/" + savedMilkId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));


        // Assert
        verify(milkService).delete(savedMilkId);
    }

    @Test
    void deleteMilk_returns404_whenMissing() throws Exception {
        // Arrange
        var savedMilkId = savedMilk.getId();
        willThrow(NotFoundException.class).given(milkService).delete(savedMilkId);

        // Act
        mockMvc.perform(delete(ApiPaths.MILKS + "/" + savedMilkId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Assert
        verify(milkService).delete(savedMilkId);
    }
}
