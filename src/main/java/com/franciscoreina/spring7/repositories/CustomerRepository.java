package com.franciscoreina.spring7.repositories;

import com.franciscoreina.spring7.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
}
