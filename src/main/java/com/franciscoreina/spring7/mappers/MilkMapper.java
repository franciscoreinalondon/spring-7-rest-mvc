package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.dto.request.milk.MilkCreateRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkUpdateRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(uses = {CategoryMapper.class})
public interface MilkMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Milk toEntity(MilkCreateRequest milkCreateRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Milk target, MilkUpdateRequest milkUpdateRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchEntity(@MappingTarget Milk target, MilkPatchRequest milkPatchRequest);

    MilkResponse toResponse(Milk milk);

}
