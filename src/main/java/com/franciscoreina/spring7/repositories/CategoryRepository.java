package com.franciscoreina.spring7.repositories;

import com.franciscoreina.spring7.domain.milk.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
