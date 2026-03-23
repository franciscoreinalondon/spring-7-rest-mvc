package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.request.milk.MilkRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MilkService {

    MilkResponse create(MilkRequest request);

    MilkResponse getById(UUID milkId);

    Page<MilkResponse> list(String name, MilkType milkType, Pageable pageable);

    void update(UUID milkId, MilkRequest request);

    void patch(UUID milkId, MilkPatchRequest request);

    void delete(UUID milkId);

}
