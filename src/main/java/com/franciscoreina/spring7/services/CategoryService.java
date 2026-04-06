package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.dto.response.milk.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    Page<CategoryResponse> search(String description, Pageable pageable);
}
