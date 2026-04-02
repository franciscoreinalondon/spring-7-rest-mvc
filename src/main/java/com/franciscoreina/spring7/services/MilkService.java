package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.request.milk.MilkRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MilkService {

    MilkResponse create(@Valid MilkRequest request);

    MilkResponse getById(UUID milkId);

    Page<MilkResponse> search(String name, MilkType milkType, Pageable pageable);

    MilkResponse update(UUID milkId, @Valid MilkRequest request);

    MilkResponse patch(UUID milkId, @Valid MilkPatchRequest request);

    void delete(UUID milkId);

}
