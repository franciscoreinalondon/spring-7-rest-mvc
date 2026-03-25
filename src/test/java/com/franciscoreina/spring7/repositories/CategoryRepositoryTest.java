package com.franciscoreina.spring7.repositories;

import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CategoryRepositoryTest {
    
    @Autowired
    CategoryRepository categoryRepository;
    
    @Autowired
    MilkRepository milkRepository;

    Milk milk;

    @BeforeEach
    void setUp() {
        milk = milkRepository.findAll().getFirst();
    }

    @Transactional
    @Test
    void testAddCategory() {
        // Act
        Category savedCategory = categoryRepository.save(
                Category.createCategory("Category description"));

        milk.addCategory(savedCategory);
        Milk savedMilk = milkRepository.save(milk);

        // Assert
        assertThat(savedMilk.getCategories().contains(savedCategory));
    }
}