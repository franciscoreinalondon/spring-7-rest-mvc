package com.franciscoreina.spring7.repositories;

import com.franciscoreina.spring7.domain.customer.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Page<Customer> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Customer> findAllByEmailIgnoreCase(String email, Pageable pageable);

    Page<Customer> findAllByNameContainingIgnoreCaseAndEmailIgnoreCase(String name, String email, Pageable pageable);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Customer> findByEmailIgnoreCase(String email);
}
