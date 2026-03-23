package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.dto.response.milk.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);
}
