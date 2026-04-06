package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.dto.response.milk.CategoryResponse;
import com.franciscoreina.spring7.mappers.CategoryMapper;
import com.franciscoreina.spring7.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // --------------------
    //  SERVICE OPERATIONS
    // --------------------

    @Override
    public Page<CategoryResponse> search(String description, Pageable pageable) {
        var cleanDescription = normalizeFilter(description);

        log.debug("Listing categories with filters: description={}", cleanDescription);

        if (cleanDescription != null) {
            return categoryRepository.findByDescriptionContainingIgnoreCase(cleanDescription, pageable)
                    .map(categoryMapper::toResponse);
        }

        return categoryRepository.findAll(pageable)
                .map(categoryMapper::toResponse);
    }

    // -----------------
    //  PRIVATE HELPERS
    // -----------------

    private String normalizeFilter(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
