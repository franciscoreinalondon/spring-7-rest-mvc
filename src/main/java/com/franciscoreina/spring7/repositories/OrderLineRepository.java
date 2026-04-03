package com.franciscoreina.spring7.repositories;

import com.franciscoreina.spring7.domain.order.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderLineRepository extends JpaRepository<OrderLine, UUID> {

    long countByMilkOrderId(UUID milkOrderId);
}
