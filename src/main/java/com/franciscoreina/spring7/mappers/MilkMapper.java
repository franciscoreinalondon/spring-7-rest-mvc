package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.dto.request.milk.MilkRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Set;

@Mapper(uses = {CategoryMapper.class})
public interface MilkMapper {

//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "version", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    Milk toEntity(MilkCreateRequest milkCreateRequest);

    default Milk toEntity(MilkRequest request, Set<Category> initialCategories) {
        if (request == null) return null;

        return Milk.createMilk(
                request.name(),
                request.milkType(),
                request.upc(),
                request.price(),
                request.stock(),
                initialCategories
        );
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Milk target, MilkRequest milkRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchEntity(@MappingTarget Milk target, MilkPatchRequest milkPatchRequest);

    MilkResponse toResponse(Milk milk);

}
