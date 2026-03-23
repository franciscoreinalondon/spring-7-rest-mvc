package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.request.milk.MilkRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.mappers.MilkMapper;
import com.franciscoreina.spring7.repositories.CategoryRepository;
import com.franciscoreina.spring7.repositories.MilkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MilkServiceImpl implements MilkService {

    private final CategoryRepository categoryRepository;
    private final MilkRepository milkRepository;
    private final MilkMapper milkMapper;

    @Override
    public MilkResponse create(MilkRequest request) {
        var initialCategories = new HashSet<>(categoryRepository.findAllById(request.categoryIds()));
        if (initialCategories.size() != request.categoryIds().size()) {
            throw new RuntimeException("One or more categories not found");
        }

        var savedMilk = milkRepository.save(milkMapper.toEntity(request, initialCategories));
        return milkMapper.toResponse(savedMilk);
    }

    @Override
    public MilkResponse getById(UUID milkId) {
        var savedMilk = getMilkOrThrow(milkId);
        return milkMapper.toResponse(savedMilk);
    }

    @Override
    public Page<MilkResponse> list(String name, MilkType milkType, Pageable pageable) {
        if (name != null && milkType != null) { // Search by name and milkType
            return milkRepository.findAllByNameContainingIgnoreCaseAndMilkType(name, milkType, pageable)
                    .map(milkMapper::toResponse);
        }

        if (name != null) { // Search by name
            return milkRepository.findAllByNameContainingIgnoreCase(name, pageable)
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
    public void update(UUID milkId, MilkRequest request) {
        var milkToUpdate = getMilkOrThrow(milkId);
        var categories = new HashSet<>(categoryRepository.findAllById(request.categoryIds()));
        if (categories.size() != request.categoryIds().size()) {
            throw new RuntimeException("One or more categories not found");
        }
        milkMapper.updateEntity(milkToUpdate, request, categories);
        milkRepository.save(milkToUpdate);
    }

    @Transactional
    @Override
    public void patch(UUID milkId, MilkPatchRequest request) {
        var milkToPatch = getMilkOrThrow(milkId);
        Set<Category> categories = null;
        if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
            categories = new HashSet<>(categoryRepository.findAllById(request.categoryIds()));
            if (categories.size() != request.categoryIds().size()) {
                throw new RuntimeException("One or more categories not found");
            }
        }

        milkMapper.patchEntity(milkToPatch, request, categories);
        milkRepository.save(milkToPatch);
    }

    @Override
    public void delete(UUID milkId) {
        var savedMilk = getMilkOrThrow(milkId);
        milkRepository.delete(savedMilk);
    }

    private Milk getMilkOrThrow(UUID milkId) {
        return milkRepository.findById(milkId)
                .orElseThrow(() -> new NotFoundException("Milk not found: " + milkId));
    }
}
