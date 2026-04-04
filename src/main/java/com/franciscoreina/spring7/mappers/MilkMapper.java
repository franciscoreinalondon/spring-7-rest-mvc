package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Set;
import java.util.UUID;

@Mapper
public interface MilkMapper {

    default void updateEntity(@MappingTarget Milk target, MilkRequest request, Set<Category> newCategories) {
        if (target == null || request == null || newCategories == null) return;

        target.renameTo(request.name());
        target.updateMilkType(request.milkType());
        target.updateUpc(request.upc());
        target.updatePrice(request.price());
        target.updateStock(request.stock());
        target.replaceCategories(newCategories);
    }

    default void patchEntity(@MappingTarget Milk target, MilkPatchRequest request, Set<Category> newCategories) {
        if (target == null || request == null) return;

        if (request.name() != null) target.renameTo(request.name());
        if (request.milkType() != null) target.updateMilkType(request.milkType());
        if (request.upc() != null) target.updateUpc(request.upc());
        if (request.price() != null) target.updatePrice(request.price());
        if (request.stock() != null) target.updateStock(request.stock());
        if (newCategories != null) target.replaceCategories(newCategories); // null = no update, [] = invalid request
    }

    @Mapping(target = "categoryIds", source = "categories")
    MilkResponse toResponse(Milk milk);

    /**
     * MapStruct automatically identifies and applies this helper method by matching
     * the source (Category) and target (UUID) types required to resolve the collection mapping.
     */
    default UUID mapCategoryToId(Category category) {
        return category == null ? null : category.getId();
    }
}
