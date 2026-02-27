package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.dtos.milk.MilkCreateRequest;
import com.franciscoreina.spring7.dtos.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dtos.milk.MilkResponse;
import com.franciscoreina.spring7.dtos.milk.MilkUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface MilkService {

    MilkResponse create(MilkCreateRequest request);

    MilkResponse getById(UUID milkId);

    List<MilkResponse> list();

    void update(UUID milkId, MilkUpdateRequest request);

    void patch(UUID milkId, MilkPatchRequest request);

    void delete(UUID milkId);

}
