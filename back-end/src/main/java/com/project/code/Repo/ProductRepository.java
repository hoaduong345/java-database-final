package com.project.code.Repo;

import com.project.code.Model.Product; // Adjust import based on your actual Product entity package
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(String category);

    @Query("SELECT DISTINCT p FROM Product p JOIN p.inventory i WHERE i.store.id = :storeId AND p.category = :category")
    List<Product> findByStoreIdAndCategory(@Param("storeId") Long storeId,
            @Param("category") String category);

    // 2. Custom query method to retrieve products within a price range
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    // 2. Custom query method to retrieve a product by its SKU
    Product findBySku(String sku);

    // 2. Custom query method to retrieve a product by its name
    Product findByName(String name);

    // 2. Custom query method using @Query to find products by a name pattern for a
    // specific store
    @Query("SELECT DISTINCT p FROM Product p JOIN p.inventory i WHERE i.store.id = :storeId AND p.name LIKE %:pname%")
    List<Product> findByNameLike(@Param("storeId") Long storeId, @Param("pname") String pname);

}
