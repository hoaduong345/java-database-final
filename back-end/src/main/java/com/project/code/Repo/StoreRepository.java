package com.project.code.Repo;

import com.project.code.Model.Store; // Adjust import based on your actual Store entity package
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    // 2. Custom query method to retrieve a store by its ID
    // (Note: JpaRepository normally provides findById returning Optional<Store>, 
    // but this matches your requested signature returning Store directly)
    Store findById(Long id);

    // 2. Custom query method using @Query to retrieve stores whose name contains a given substring
    @Query("SELECT s FROM Store s WHERE s.name LIKE %:pname%")
    List<Store> findBySubName(@Param("pname") String pname);

}