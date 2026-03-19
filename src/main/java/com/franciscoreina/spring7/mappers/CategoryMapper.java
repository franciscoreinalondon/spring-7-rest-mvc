package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import org.mapstruct.Mapper;

@Mapper
public interface CategoryMapper {

    MilkResponse toResponse(Milk milk);

}
