package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.dto.response.milk.CategoryResponse;
import com.franciscoreina.spring7.services.CategoryService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ImportAutoConfiguration(exclude = OAuth2ResourceServerAutoConfiguration.class)
@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CategoryService categoryService;

    @Nested
    class SearchTests {

        @Test
        void search_shouldReturnPage_whenNoFiltersAreProvided() throws Exception {
            // Arrange
            var response1 = new CategoryResponse(UUID.randomUUID(), Instant.parse("2026-04-05T10:00:00Z"), "Milk");
            var response2 = new CategoryResponse(UUID.randomUUID(), Instant.parse("2026-04-05T10:00:00Z"), "Cheese");
            var page = new PageImpl<>(List.of(response1, response2), PageRequest.of(0, 20), 2);

            given(categoryService.search(null, PageRequest.of(0, 20))).willReturn(page);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.CATEGORIES)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].id").value(response1.id().toString()))
                    .andExpect(jsonPath("$.content[0].description").value(response1.description()))
                    .andExpect(jsonPath("$.content[1].id").value(response2.id().toString()))
                    .andExpect(jsonPath("$.content[1].description").value(response2.description()));

            verify(categoryService).search(null, PageRequest.of(0, 20));
        }

        @Test
        void search_shouldReturnPage_whenFiltersAreProvided() throws Exception {
            // Arrange
            var response = new CategoryResponse(UUID.randomUUID(), Instant.parse("2026-04-05T10:00:00Z"), "Milk");
            var page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);

            given(categoryService.search("Milk", PageRequest.of(0, 20))).willReturn(page);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.CATEGORIES)
                            .param("description", "Milk"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].id").value(response.id().toString()))
                    .andExpect(jsonPath("$.content[0].description").value(response.description()));

            verify(categoryService).search("Milk", PageRequest.of(0, 20));
        }

        @Test
        void search_shouldReturnNoResults_whenEmptyPage() throws Exception {
            // Arrange
            var page = new PageImpl<CategoryResponse>(List.of(), PageRequest.of(0, 20), 0);

            given(categoryService.search("Milk", PageRequest.of(0, 20))).willReturn(page);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.CATEGORIES)
                            .param("description", "Milk")
                            .with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0));

            verify(categoryService).search("Milk", PageRequest.of(0, 20));
        }
    }
}
