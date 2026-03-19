package com.franciscoreina.spring7.repositories;

import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MilkRepository extends JpaRepository<Milk, UUID> {

    Page<Milk> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Milk> findAllByMilkType(MilkType milkType, Pageable pageable);

    Page<Milk> findAllByNameContainingIgnoreCaseAndMilkType(String name, MilkType milkType, Pageable pageable);

}
