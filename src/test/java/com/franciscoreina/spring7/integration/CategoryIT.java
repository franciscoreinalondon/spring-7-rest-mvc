package com.franciscoreina.spring7.integration;

import com.franciscoreina.spring7.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CategoryIT extends AbstractJwtMockIntegrationTest {
//
//    @Autowired
//    CategoryRepository categoryRepository;
//
//    @BeforeEach
//    void setUpData() {
//        categoryRepository.deleteAll();
//
//        categoryRepository.save(Category.builder()
//                .description("Milk")
//                .build());
//
//        categoryRepository.save(Category.builder()
//                .description("Cheese")
//                .build());
//    }
//
//    @Test
//    void shouldReturnAllCategories() {
//        getRequest("/api/v1/categories")
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.content.length()").isEqualTo(2);
//    }
//
//    @Test
//    void shouldFilterByDescription() {
//        getRequest("/api/v1/categories?description=Milk")
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.content.length()").isEqualTo(1)
//                .jsonPath("$.content[0].description").isEqualTo("Milk");
//    }
//
//    @Test
//    void shouldReturnEmptyWhenNoMatch() {
//        getRequest("/api/v1/categories?description=Beer")
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.content.length()").isEqualTo(0);
//    }
}
