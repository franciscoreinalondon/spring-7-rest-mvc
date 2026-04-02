package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import com.franciscoreina.spring7.exceptions.ConflictException;
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

    // --------------------
    //  SERVICE OPERATIONS
    // --------------------

    @Transactional
    @Override
    public MilkResponse create(MilkRequest request) {
        log.info("Creating milk with upc={}", request.upc());

        assertUpcNotInUse(request.upc());

        var categories = fetchRequiredCategoriesOrThrow(request.categoryIds());
        var milk = milkMapper.toEntity(request, categories);

        return milkMapper.toResponse(milkRepository.save(milk));
    }

    @Override
    public MilkResponse getById(UUID milkId) {
        return milkMapper.toResponse(findMilkOrThrow(milkId));
    }

    @Override
    public Page<MilkResponse> search(String name, MilkType milkType, Pageable pageable) {
        var cleanName = normalizeFilter(name);

        log.debug("Listing milks with filters: name={}, milkType={}, page={}, size={}",
                cleanName, milkType, pageable.getPageNumber(), pageable.getPageSize());


        if (cleanName != null && milkType != null) {
            return milkRepository.findAllByNameContainingIgnoreCaseAndMilkType(cleanName, milkType, pageable)
                    .map(milkMapper::toResponse);
        }

        if (cleanName != null) {
            return milkRepository.findAllByNameContainingIgnoreCase(cleanName, pageable)
                    .map(milkMapper::toResponse);
        }

        if (milkType != null) {
            return milkRepository.findAllByMilkType(milkType, pageable)
                    .map(milkMapper::toResponse);
        }

        return milkRepository.findAll(pageable)
                .map(milkMapper::toResponse);
    }

    @Transactional
    @Override
    public MilkResponse update(UUID milkId, MilkRequest request) {
        log.info("Updating milk id={}", milkId);

        var milk = findMilkOrThrow(milkId);

        if (!milk.getUpc().equalsIgnoreCase(request.upc())) {
            assertUpcNotInUseByAnotherMilk(milkId, request.upc());
        }

        var categories = fetchRequiredCategoriesOrThrow(request.categoryIds());

        milkMapper.updateEntity(milk, request, categories);
        return milkMapper.toResponse(milk);
    }

    @Transactional
    @Override
    public MilkResponse patch(UUID milkId, MilkPatchRequest request) {
        log.info("Patching milk id={}", milkId);

        var milk = findMilkOrThrow(milkId);

        if (request.name() == null && request.milkType() == null && request.upc() == null
                && request.price() == null && request.stock() == null && request.categoryIds() == null) {
            return milkMapper.toResponse(milk);
        }

        if (request.upc() != null && !milk.getUpc().equalsIgnoreCase(request.upc())) {
            assertUpcNotInUseByAnotherMilk(milkId, request.upc());
        }

        var categories = fetchPatchCategoriesOrThrow(request.categoryIds());

        milkMapper.patchEntity(milk, request, categories);
        return milkMapper.toResponse(milk);
    }

    @Transactional
    @Override
    public void delete(UUID milkId) {
        log.info("Deleting milk id={}", milkId);

        var milk = findMilkOrThrow(milkId);
        milkRepository.delete(milk);
    }

    // -----------------
    //  PRIVATE HELPERS
    // -----------------

    private void assertUpcNotInUse(String upc) {
        if (milkRepository.existsByUpcIgnoreCase(upc.trim())) {
            throw new ConflictException("Milk UPC already exists: " + upc);
        }
    }

    private void assertUpcNotInUseByAnotherMilk(UUID milkId, String upc) {
        var existing = milkRepository.findByUpcIgnoreCase(upc.trim());

        if (existing.isPresent() && !existing.get().getId().equals(milkId)) {
            throw new ConflictException("Milk UPC already exists: " + upc);
        }
    }

    private Set<Category> fetchRequiredCategoriesOrThrow(Set<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new IllegalArgumentException("At least one category is required");
        }

        var categories = new HashSet<>(categoryRepository.findAllById(categoryIds));
        if (categories.size() != categoryIds.size()) {
            throw new NotFoundException("One or more categories not found");
        }
        return categories;
    }

    private Set<Category> fetchPatchCategoriesOrThrow(Set<UUID> categoryIds) {
        if (categoryIds == null) return null;
        if (categoryIds.isEmpty()) throw new IllegalArgumentException("At least one category is required");

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

    private String normalizeFilter(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
