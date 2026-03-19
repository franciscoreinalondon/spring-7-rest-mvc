package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.request.milk.MilkCreateRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import com.franciscoreina.spring7.dto.request.milk.MilkUpdateRequest;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.mappers.MilkMapper;
import com.franciscoreina.spring7.repositories.MilkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MilkServiceImpl implements MilkService {

    private final MilkRepository milkRepository;
    private final MilkMapper milkMapper;

    @Override
    public MilkResponse create(MilkCreateRequest request) {
        Milk saved = milkRepository.save(milkMapper.toEntity(request));
        return milkMapper.toResponse(saved);
    }

    @Override
    public MilkResponse getById(UUID milkId) {
        Milk milk = getMilkOrThrow(milkId);
        return milkMapper.toResponse(milk);
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
    public void update(UUID milkId, MilkUpdateRequest request) {
        Milk milkToUpdate = getMilkOrThrow(milkId);
        milkMapper.updateEntity(milkToUpdate, request);
        milkRepository.save(milkToUpdate);
    }

    @Transactional
    @Override
    public void patch(UUID milkId, MilkPatchRequest request) {
        Milk milkToPatch = getMilkOrThrow(milkId);
        milkMapper.patchEntity(milkToPatch, request);
        milkRepository.save(milkToPatch);
    }

    @Override
    public void delete(UUID milkId) {
        Milk milk = getMilkOrThrow(milkId);
        milkRepository.delete(milk);
    }

    private Milk getMilkOrThrow(UUID milkId) {
        return milkRepository.findById(milkId)
                .orElseThrow(() -> new NotFoundException("Milk not found: " + milkId));
    }
}
