package com.project.code.Repo;

import com.project.code.Model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Customer findByEmail(String email);

    // Use findByid to avoid conflicting with JpaRepository's
    // inherited findById method, which returns Optional<Customer>.
    Customer findByid(Long id);

    List<Customer> findByName(String name);
}
