package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.mappers.MilkMapper;
import com.franciscoreina.spring7.repositories.CategoryRepository;
import com.franciscoreina.spring7.repositories.MilkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class MilkServiceImpl implements MilkService {

    private final CategoryRepository categoryRepository;
    private final MilkRepository milkRepository;
    private final MilkMapper milkMapper;

    @Transactional
    @Override
    public MilkResponse create(MilkRequest request) {
        log.info("Creating milk: {}", request.upc());
        var categories = fetchCategoriesOrThrow(request.categoryIds());
        var milk = milkMapper.toEntity(request, categories);
        return milkMapper.toResponse(milkRepository.save(milk));
    }

    @Override
    public MilkResponse getById(UUID milkId) {
        return milkMapper.toResponse(findMilkOrThrow(milkId));
    }

    @Override
    public Page<MilkResponse> list(String name, MilkType milkType, Pageable pageable) {
        var cleanName = (name != null && !name.isBlank()) ? name : null;

        if (cleanName != null && milkType != null) { // Search by name and milkType
            return milkRepository.findAllByNameContainingIgnoreCaseAndMilkType(cleanName, milkType, pageable)
                    .map(milkMapper::toResponse);
        }

        if (cleanName != null) { // Search by name
            return milkRepository.findAllByNameContainingIgnoreCase(cleanName, pageable)
                    .map(milkMapper::toResponse);
        }

        if (milkType != null) { // Search by milkType
            return milkRepository.findAllByMilkType(milkType, pageable)
                    .map(milkMapper::toResponse);
        }

        return milkRepository.findAll(pageable) // Search all
                .map(milkMapper::toResponse);
    }

    @Transactional
    @Override
    public MilkResponse update(UUID milkId, MilkRequest request) {
        log.info("Updating milk id: {}", milkId);
        var milk = findMilkOrThrow(milkId);
        var categories = fetchCategoriesOrThrow(request.categoryIds());
        milkMapper.updateEntity(milk, request, categories);
        // Hibernate save automatically at the end of the transaction (Dirty Checking)
        return milkMapper.toResponse(milk);
    }

    @Transactional
    @Override
    public MilkResponse patch(UUID milkId, MilkPatchRequest request) {
        log.info("Patching milk id: {}", milkId);
        var milk = findMilkOrThrow(milkId);
        Set<Category> categories = (request.categoryIds() != null && !request.categoryIds().isEmpty())
                ? fetchCategoriesOrThrow(request.categoryIds()) : null;
        milkMapper.patchEntity(milk, request, categories);
        // Hibernate save automatically at the end of the transaction (Dirty Checking)
        return milkMapper.toResponse(milk);
    }

    @Transactional
    @Override
    public void delete(UUID milkId) {
        log.info("Deleting milk id: {}", milkId);
        var savedMilk = findMilkOrThrow(milkId);
        milkRepository.delete(savedMilk);
    }

    private Set<Category> fetchCategoriesOrThrow(Set<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return Collections.emptySet();

        var categories = new HashSet<>(categoryRepository.findAllById(categoryIds));
        if (categories.size() != categoryIds.size()) {
            throw new NotFoundException("One or more categories not found");
        }
        return categories;
    }

    private Milk findMilkOrThrow(UUID milkId) {
        return milkRepository.findById(milkId)
                .orElseThrow(() -> new NotFoundException("Milk not found: " + milkId));
    }
}
