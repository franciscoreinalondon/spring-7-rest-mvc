package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import com.franciscoreina.spring7.services.MilkService;
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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.mockito.BDDMockito.given;
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
class MilkControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private MilkService milkService;

    private static final UUID MILK_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID_1 = UUID.randomUUID();
    private static final UUID CATEGORY_ID_2 = UUID.randomUUID();

    private static final LinkedHashSet<UUID> CATEGORY_IDS =
            new LinkedHashSet<>(Arrays.asList(CATEGORY_ID_1, CATEGORY_ID_2));

    private static final Model<MilkRequest> REQUEST_MODEL = Instancio.of(MilkRequest.class)
            .set(field(MilkRequest::name), "Whole Milk")
            .set(field(MilkRequest::milkType), MilkType.WHOLE)
            .set(field(MilkRequest::upc), "UPC123")
            .set(field(MilkRequest::price), new BigDecimal("2.50"))
            .set(field(MilkRequest::stock), 10)
            .set(field(MilkRequest::categoryIds), CATEGORY_IDS)
            .toModel();

    private static final Model<MilkPatchRequest> PATCH_MODEL = Instancio.of(MilkPatchRequest.class)
            .set(field(MilkPatchRequest::name), "Patched Milk")
            .set(field(MilkPatchRequest::milkType), MilkType.SKIMMED)
            .set(field(MilkPatchRequest::upc), "PATCH123")
            .set(field(MilkPatchRequest::price), new BigDecimal("3.75"))
            .set(field(MilkPatchRequest::stock), 20)
            .set(field(MilkPatchRequest::categoryIds), CATEGORY_IDS)
            .toModel();

    @Nested
    class CreateTests {

        @Test
        void create_shouldReturnCreated_whenRequestIsValid() throws Exception {
            // Arrange
            var request = Instancio.create(REQUEST_MODEL);
            var response = response(
                    request.name(),
                    request.milkType(),
                    request.upc(),
                    request.price(),
                    request.stock(),
                    request.categoryIds()
            );

            given(milkService.create(request)).willReturn(response);

            // Act + Assert
            mockMvc.perform(post(ApiPaths.MILKS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION, ApiPaths.MILKS + "/" + MILK_ID))
                    .andExpect(jsonPath("$.id").value(response.id().toString()))
                    .andExpect(jsonPath("$.name").value(response.name()))
                    .andExpect(jsonPath("$.milkType").value(response.milkType().name()))
                    .andExpect(jsonPath("$.upc").value(response.upc()))
                    .andExpect(jsonPath("$.price").value(response.price().doubleValue()))
                    .andExpect(jsonPath("$.stock").value(response.stock()))
                    .andExpect(jsonPath("$.categoryIds[0]").value(CATEGORY_ID_1.toString()))
                    .andExpect(jsonPath("$.categoryIds[1]").value(CATEGORY_ID_2.toString()));

            verify(milkService).create(request);
        }

        @Test
        void create_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
            // Arrange
            var request = new MilkRequest(
                    "",
                    null,
                    "",
                    null,
                    -1,
                    Set.of()
            );

            // Act + Assert
            mockMvc.perform(post(ApiPaths.MILKS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(milkService);
        }

        @Test
        void create_shouldReturnBadRequest_whenNameIsNull() throws Exception {
            // Arrange
            var request = new MilkRequest(
                    null,
                    MilkType.WHOLE,
                    "UPC123",
                    new BigDecimal("2.50"),
                    10,
                    CATEGORY_IDS
            );

            // Act + Assert
            mockMvc.perform(post(ApiPaths.MILKS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists());

            verifyNoInteractions(milkService);
        }

        @Test
        void create_shouldReturnBadRequest_whenUpcIsNull() throws Exception {
            // Arrange
            var request = new MilkRequest(
                    "Whole Milk",
                    MilkType.WHOLE,
                    null,
                    new BigDecimal("2.50"),
                    10,
                    CATEGORY_IDS
            );

            // Act + Assert
            mockMvc.perform(post(ApiPaths.MILKS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.upc").exists());

            verifyNoInteractions(milkService);
        }
    }

    @Nested
    class GetByIdTests {

        @Test
        void getById_shouldReturnOk_whenMilkExists() throws Exception {
            // Arrange
            var response = response("Whole Milk", MilkType.WHOLE, "UPC123", new BigDecimal("2.50"), 10, CATEGORY_IDS);

            given(milkService.getById(MILK_ID)).willReturn(response);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.MILKS + "/" + MILK_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(response.id().toString()))
                    .andExpect(jsonPath("$.name").value(response.name()))
                    .andExpect(jsonPath("$.milkType").value(response.milkType().name()))
                    .andExpect(jsonPath("$.upc").value(response.upc()))
                    .andExpect(jsonPath("$.price").value(response.price().doubleValue()))
                    .andExpect(jsonPath("$.stock").value(response.stock()));

            verify(milkService).getById(MILK_ID);
        }
    }

    @Nested
    class SearchTests {

        @Test
        void search_shouldReturnPage_whenNoFiltersAreProvided() throws Exception {
            // Arrange
            var response = response("Whole Milk", MilkType.WHOLE, "UPC123", new BigDecimal("2.50"), 10, CATEGORY_IDS);
            var page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);

            given(milkService.search(null, null, PageRequest.of(0, 20))).willReturn(page);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.MILKS)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(response.id().toString()))
                    .andExpect(jsonPath("$.content[0].name").value(response.name()))
                    .andExpect(jsonPath("$.content[0].milkType").value(response.milkType().name()))
                    .andExpect(jsonPath("$.content[0].upc").value(response.upc()));

            verify(milkService).search(null, null, PageRequest.of(0, 20));
        }

        @Test
        void search_shouldReturnPage_whenNameFilterIsProvided() throws Exception {
            // Arrange
            var response = response("Whole Milk", MilkType.WHOLE, "UPC123", new BigDecimal("2.50"), 10, CATEGORY_IDS);
            var page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);

            given(milkService.search("Whole", null, PageRequest.of(0, 20))).willReturn(page);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.MILKS)
                            .param("name", "Whole")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(response.id().toString()));

            verify(milkService).search("Whole", null, PageRequest.of(0, 20));
        }

        @Test
        void search_shouldReturnPage_whenMilkTypeFilterIsProvided() throws Exception {
            // Arrange
            var response = response("Whole Milk", MilkType.WHOLE, "UPC123", new BigDecimal("2.50"), 10, CATEGORY_IDS);
            var page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);

            given(milkService.search(null, MilkType.WHOLE, PageRequest.of(0, 20))).willReturn(page);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.MILKS)
                            .param("milkType", "WHOLE")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(response.id().toString()));

            verify(milkService).search(null, MilkType.WHOLE, PageRequest.of(0, 20));
        }

        @Test
        void search_shouldReturnPage_whenBothFiltersAreProvided() throws Exception {
            // Arrange
            var response = response("Whole Milk", MilkType.WHOLE, "UPC123", new BigDecimal("2.50"), 10, CATEGORY_IDS);
            var page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);

            given(milkService.search("Whole", MilkType.WHOLE, PageRequest.of(0, 20))).willReturn(page);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.MILKS)
                            .param("name", "Whole")
                            .param("milkType", "WHOLE")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(response.id().toString()));

            verify(milkService).search("Whole", MilkType.WHOLE, PageRequest.of(0, 20));
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void update_shouldReturnOk_whenRequestIsValid() throws Exception {
            // Arrange
            var request = Instancio.create(REQUEST_MODEL);
            var response = response(
                    request.name(),
                    request.milkType(),
                    request.upc(),
                    request.price(),
                    request.stock(),
                    request.categoryIds()
            );

            given(milkService.update(MILK_ID, request)).willReturn(response);

            // Act + Assert
            mockMvc.perform(put(ApiPaths.MILKS + "/" + MILK_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(response.id().toString()))
                    .andExpect(jsonPath("$.name").value(response.name()))
                    .andExpect(jsonPath("$.milkType").value(response.milkType().name()))
                    .andExpect(jsonPath("$.upc").value(response.upc()))
                    .andExpect(jsonPath("$.price").value(response.price().doubleValue()))
                    .andExpect(jsonPath("$.stock").value(response.stock()));

            verify(milkService).update(MILK_ID, request);
        }

        @Test
        void update_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
            // Arrange
            var request = new MilkRequest(
                    "",
                    null,
                    "",
                    null,
                    -1,
                    Set.of()
            );

            // Act + Assert
            mockMvc.perform(put(ApiPaths.MILKS + "/" + MILK_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(milkService);
        }
    }

    @Nested
    class PatchTests {

        @Test
        void patch_shouldReturnOk_whenPatchIsEmpty() throws Exception {
            // Arrange
            var request = new MilkPatchRequest(null, null, null, null, null, null);
            var response = response("Whole Milk", MilkType.WHOLE, "UPC123", new BigDecimal("2.50"), 10, CATEGORY_IDS);

            given(milkService.patch(MILK_ID, request)).willReturn(response);

            // Act + Assert
            mockMvc.perform(patch(ApiPaths.MILKS + "/" + MILK_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(response.id().toString()));

            verify(milkService).patch(MILK_ID, request);
        }

        @Test
        void patch_shouldReturnOk_whenRequestIsValid() throws Exception {
            // Arrange
            var request = Instancio.create(PATCH_MODEL);
            var response = response(
                    request.name(),
                    request.milkType(),
                    request.upc(),
                    request.price(),
                    request.stock(),
                    request.categoryIds()
            );

            given(milkService.patch(MILK_ID, request)).willReturn(response);

            // Act + Assert
            mockMvc.perform(patch(ApiPaths.MILKS + "/" + MILK_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(response.id().toString()))
                    .andExpect(jsonPath("$.name").value(response.name()))
                    .andExpect(jsonPath("$.milkType").value(response.milkType().name()))
                    .andExpect(jsonPath("$.upc").value(response.upc()))
                    .andExpect(jsonPath("$.price").value(response.price().doubleValue()))
                    .andExpect(jsonPath("$.stock").value(response.stock()));

            verify(milkService).patch(MILK_ID, request);
        }

        @Test
        void patch_shouldReturnBadRequest_whenFieldsAreInvalid() throws Exception {
            // Arrange
            var request = new MilkPatchRequest(" ", null, " ", new BigDecimal("-1.00"), -1, Set.of());

            // Act + Assert
            mockMvc.perform(patch(ApiPaths.MILKS + "/" + MILK_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(milkService);
        }
    }

    @Nested
    class DeleteTests {

        @Test
        void delete_shouldReturnNoContent_whenMilkExists() throws Exception {
            // Act + Assert
            mockMvc.perform(delete(ApiPaths.MILKS + "/" + MILK_ID))
                    .andExpect(status().isNoContent());

            verify(milkService).delete(MILK_ID);
        }
    }

    private MilkResponse response(String name,
                                  MilkType milkType,
                                  String upc,
                                  BigDecimal price,
                                  Integer stock,
                                  Set<UUID> categoryIds) {
        return new MilkResponse(
                MILK_ID,
                Instant.parse("2026-04-03T10:00:00Z"),
                Instant.parse("2026-04-03T10:00:00Z"),
                name,
                milkType,
                upc,
                price,
                stock,
                categoryIds
        );
    }
}
