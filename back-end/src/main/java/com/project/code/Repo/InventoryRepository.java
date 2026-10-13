package com.project.code.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.code.Model.Inventory;

import jakarta.transaction.Transactional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query("""
            SELECT i FROM Inventory i
            WHERE i.product.id = :productId
            AND i.store.id = :storeId
            """)
    Inventory findByProductIdAndStoreId(
            @Param("productId") Long productId,
            @Param("storeId") Long storeId);

    @Query("SELECT i FROM Inventory i WHERE i.store.id = :storeId")
    List<Inventory> findByStoreId(@Param("storeId") Long storeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Inventory i WHERE i.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
