package com.franciscoreina.spring7.repositories;

import com.franciscoreina.spring7.domain.order.MilkOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MilkOrderRepository extends JpaRepository<MilkOrder, UUID> {

    Page<MilkOrder> findAllByCustomerRefContainingIgnoreCase(String customerRef, Pageable pageable);
}
