package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.services.MilkService;
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

import java.math.BigDecimal;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    // ---------------
    //    POSITIVE
    // ---------------

    @Nested
    class PositiveTests {

        @Test
        void postMilk_returns201_whenValidRequest() throws Exception {
            // Arrange
            var request = Instancio.create(MilkRequest.class);
            var response = Instancio.create(MilkResponse.class);

            given(milkService.create(request)).willReturn(response);

            // Act + Assert
            mockMvc.perform(post(ApiPaths.MILKS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", ApiPaths.MILKS + "/" + response.id()))
                    .andExpect(jsonPath("$.id").value(response.id().toString()));
        }

        @Test
        void getMilkById_returns200_whenExists() throws Exception {
            // Arrange
            var response = Instancio.create(MilkResponse.class);

            given(milkService.getById(response.id())).willReturn(response);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.MILKS + "/{id}", response.id()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(response.id().toString()));
        }

        @Test
        void listMilks_returns200_withData() throws Exception {
            // Arrange
            var page = new PageImpl<>(Instancio.ofList(MilkResponse.class).size(2).create());

            given(milkService.search(any(), any(), any())).willReturn(page);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.MILKS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.size()").value(2));
        }

        @Test
        void listMilks_returns200_whenFilteredByNameAndType() throws Exception {
            var page = new PageImpl<>(Instancio.ofList(MilkResponse.class).size(2).create());

            // Arrange
            given(milkService.search(eq("Milk name"), eq(MilkType.A2), any())).willReturn(page);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.MILKS).param("name", "Milk name").param("milkType", "A2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.size()").value(2));
        }

        @Test
        void putMilk_returns200_whenExists() throws Exception {
            // Arrange
            var id = UUID.randomUUID();
            var request = Instancio.create(MilkRequest.class);
            var response = Instancio.of(MilkResponse.class).set(field(MilkResponse::id), id).create();

            given(milkService.update(id, request)).willReturn(response);

            // Act + Assert
            mockMvc.perform(put(ApiPaths.MILKS + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        void patchMilk_returns200_whenExists() throws Exception {
            // Arrange
            var id = UUID.randomUUID();
            var patch = Instancio.create(MilkPatchRequest.class);
            var response = Instancio.of(MilkResponse.class).set(field(MilkResponse::id), id).create();

            given(milkService.patch(id, patch)).willReturn(response);

            // Act + Assert
            mockMvc.perform(patch(ApiPaths.MILKS + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patch)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        void deleteMilk_returns204_whenExists() throws Exception {
            // Arrange
            var id = UUID.randomUUID();

            // Act
            mockMvc.perform(delete(ApiPaths.MILKS + "/{id}", id))
                    .andExpect(status().isNoContent());

            // Assert
            verify(milkService).delete(id);
        }
    }

    // ---------------
    //    NEGATIVE
    // ---------------

    @Nested
    class NegativeTests {

        @Test
        void postMilk_returns400_whenInvalidData() throws Exception {
            // Arrange
            var invalidRequest = new MilkRequest(null, null, "Invalid upc!!!", new BigDecimal("-1.00"), -1, null);

            // Act + Assert
            mockMvc.perform(post(ApiPaths.MILKS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists())
                    .andExpect(jsonPath("$.errors.milkType").exists())
                    .andExpect(jsonPath("$.errors.upc").exists())
                    .andExpect(jsonPath("$.errors.price").exists())
                    .andExpect(jsonPath("$.errors.stock").exists())
                    .andExpect(jsonPath("$.errors.categoryIds").exists());

            // Assert
            verifyNoInteractions(milkService);
        }

        @Test
        void postMilk_returns409_whenDuplicatedUpc() throws Exception {
            // Arrange
            var request = Instancio.create(MilkRequest.class);

            given(milkService.create(request)).willThrow(new DataIntegrityViolationException("Conflict"));

            // Act + Assert
            mockMvc.perform(post(ApiPaths.MILKS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        void getMilk_returns404_whenNotFound() throws Exception {
            // Arrange
            var id = UUID.randomUUID();

            given(milkService.getById(id)).willThrow(new NotFoundException("Not found"));

            // Act + Assert
            mockMvc.perform(get(ApiPaths.MILKS + "/{id}", id))
                    .andExpect(status().isNotFound());
        }

        @Test
        void putMilk_returns400_whenNameNull() throws Exception {
            // Arrange
            var id = UUID.randomUUID();
            var invalidRequest = new MilkRequest(null, null, "Invalid upc!!!", new BigDecimal("-1.00"), -1, null);

            // Act + Assert
            mockMvc.perform(put(ApiPaths.MILKS + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists())
                    .andExpect(jsonPath("$.errors.milkType").exists())
                    .andExpect(jsonPath("$.errors.upc").exists())
                    .andExpect(jsonPath("$.errors.price").exists())
                    .andExpect(jsonPath("$.errors.stock").exists())
                    .andExpect(jsonPath("$.errors.categoryIds").exists());

            // Assert
            verifyNoInteractions(milkService);
        }

        @Test
        void putMilk_returns404_whenNotFound() throws Exception {
            // Arrange
            var id = UUID.randomUUID();
            var request = Instancio.create(MilkRequest.class);

            given(milkService.update(id, request)).willThrow(new NotFoundException("Not found"));

            // Act + Assert
            mockMvc.perform(put(ApiPaths.MILKS + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void putMilk_returns409_whenDuplicatedUpc() throws Exception {
            // Arrange
            var id = UUID.randomUUID();
            var request = Instancio.create(MilkRequest.class);

            given(milkService.update(id, request)).willThrow(new DataIntegrityViolationException("Conflict"));

            // Act + Assert
            mockMvc.perform(put(ApiPaths.MILKS + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        void patchMilk_returns400_whenInvalidData() throws Exception {
            // Arrange
            var id = UUID.randomUUID();
            var invalidRequest = new MilkRequest(null, null, "Invalid upc!!!", new BigDecimal("-1.00"), -1, null);

            // Act + Assert
            mockMvc.perform(patch(ApiPaths.MILKS + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.upc").exists())
                    .andExpect(jsonPath("$.errors.price").exists())
                    .andExpect(jsonPath("$.errors.stock").exists());

            // Assert
            verifyNoInteractions(milkService);
        }

        @Test
        void patchMilk_returns404_whenNotFound() throws Exception {
            // Arrange
            var id = UUID.randomUUID();
            var patch = Instancio.create(MilkPatchRequest.class);

            given(milkService.patch(id, patch)).willThrow(new NotFoundException("Not found"));

            // Act + Assert
            mockMvc.perform(patch(ApiPaths.MILKS + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patch)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void patchMilk_returns409_whenUpcDuplicated() throws Exception {
            // Arrange
            var id = UUID.randomUUID();
            var invalidRequest = Instancio.create(MilkPatchRequest.class);

            given(milkService.patch(id, invalidRequest)).willThrow(new DataIntegrityViolationException("Conflict"));

            // Act + Assert
            mockMvc.perform(patch(ApiPaths.MILKS + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isConflict());
        }

        @Test
        void deleteMilk_returns404_whenNotFound() throws Exception {
            // Arrange
            var id = UUID.randomUUID();

            willThrow(new NotFoundException("Not found")).given(milkService).delete(id);

            // Act + Assert
            mockMvc.perform(delete(ApiPaths.MILKS + "/{id}", id)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            // Assert
            verify(milkService).delete(id);
        }
    }
}
